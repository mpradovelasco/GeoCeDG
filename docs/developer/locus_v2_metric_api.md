# Locus V2 metric candidate API

- Status: **G7A CHARACTERIZATION ONLY**
- Availability: **NO PRODUCTIVE METRIC EXISTS**
- Review state: **G7A-R1 AND G7A PASS — AUTHOR APPROVED**
- G7 specification: `NORMATIVE / AUTHOR APPROVED`
- ADR 0007: `Accepted`
- Date: 2026-08-13

This document describes the author-approved candidate Java API established by
the G7A and focused G7A-R1 experiments and normalized at final closeout.
It is design evidence for G7B and future monograph work. None of the classes in
this document exists in productive source yet. Names may be adjusted only to
fit the actual code while preserving the normative contracts.

## 1. Package and authority boundary

Metric semantic values should be GeoCeDG-owned:

```text
org.geocedg.common.kernel.locus.metric
```

Normal-DAG publication belongs in the existing GeoCeDG kernel packages:

```text
org.geocedg.common.kernel.geos.GeoLocusMetricResult
org.geocedg.common.kernel.algos.AlgoLocusMetricV2
org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter
```

The productive G6 `LocusDefinition2D`, provider, evaluator, semantic revision
and branch/component domain remain upstream inputs. Rendering, legacy
`GeoLocus`, `MyPoint`, viewport state and pixel tolerance are forbidden metric
dependencies.

## 2. Positions and bindings

```java
public final class LocusSemanticPosition2D {
    String getLocusIdentity();
    String getBranchKey();
    String getProviderVersion();
    double getProviderCanonicalParameter();
}
```

The position is durable semantic identity. Revision and valid-component key do
not belong in this value.

```java
public final class MetricPositionBinding2D {
    LocusSemanticPosition2D getSemanticPosition();
    long getSemanticRevision();
    String getResolvedValidComponentKey();
    MetricPositionEvaluationStatus getEvaluationStatus();
    LocusPoint2D getEvaluatedPoint();
    List<MetricDiagnostic2D> getDiagnostics();
}

public enum MetricPositionEvaluationStatus {
    VALID,
    POSITION_STALE,
    POSITION_OUTSIDE_DOMAIN,
    BRANCH_MISSING,
    PROVIDER_VERSION_MISMATCH
}
```

Component keys are deterministic and revision-scoped. Rebinding is explicit:

```java
MetricPositionBinding2D bind(
        LocusSemanticPosition2D position,
        LocusDefinition2D definition);
```

No overload may repair a position by Cartesian proximity.

## 3. Query types

Use a closed internal query hierarchy rather than an overloaded command-shaped
value:

```java
public sealed interface LocusMetricQuery2D
        permits BetweenPositionsMetricQuery, TotalLocusMetricQuery {
    String getLocusIdentity();
    long getSemanticRevision();
    LocusMetricPolicy2D getPolicy();
}
```

```java
public final class BetweenPositionsMetricQuery
        implements LocusMetricQuery2D {
    MetricPositionBinding2D getStart();
    MetricPositionBinding2D getTarget();
    TraversalDirection getDirection();
    OpenBoundaryPolicy getBoundaryPolicy();
    SamePositionPolicy getSamePositionPolicy();
}

public enum TraversalDirection {
    FORWARD,
    REVERSE
}

public enum OpenBoundaryPolicy {
    STOP_AT_END,
    WRAP_TO_START,
    STRICT
}

public enum SamePositionPolicy {
    ZERO_LENGTH,
    FULL_CYCLE
}
```

`SHORTEST` is deliberately absent from the G7B minimum candidate.

```java
public final class TotalLocusMetricQuery implements LocusMetricQuery2D {
    // Identity, revision and policy only.
}
```

The total query has no endpoint, direction, WRAP or FULL_CYCLE field.

## 4. Route API

```java
public final class LocusMetricRouteResolver2D {
    LocusMetricRoute2D resolve(
            BetweenPositionsMetricQuery query,
            LocusDefinition2D definition);
}
```

The resolver validates the query and selects a route. It never evaluates an
integral.

```java
public final class LocusMetricRoute2D {
    String getLocusIdentity();
    long getSemanticRevision();
    String getBranchKey();
    List<LocusMetricRouteSegment2D> getOrderedRouteSegments();
    TraversalDirection getDirection();
    OpenBoundaryPolicy getBoundaryPolicy();
    boolean isTargetReached();
    boolean isWrapped();
    boolean isGeometricallyConnected();
    MetricRouteStatus getRouteStatus();
    TraversalOutcome getTraversalOutcome();
    List<MetricDiagnostic2D> getDiagnostics();
}
```

```java
public final class LocusMetricRouteSegment2D {
    String getResolvedValidComponentKey();
    double getStartCanonicalParameter();
    double getEndCanonicalParameter();
    TraversalDirection getDirection();
    MetricRouteSegmentRole getRole();
}
```

Every segment lies in exactly one valid component. WRAP on an open branch has
two segments and sets `geometricallyConnected=false`.

## 5. Policy and tolerances

```java
public final class LocusMetricPolicy2D {
    double getAbsoluteTolerance();
    double getRelativeTolerance();
    MetricWorkBudget2D getWorkBudget();
    String getMetricAlgorithmVersion();
    String getMetricPolicyVersion();
    String getTolerancePolicyVersion();
    MetricMultiplicityPolicy getMultiplicityPolicy();
    ImproperLimitPolicy2D getImproperLimitPolicy();
    EvaluatorOnlyPolicy getEvaluatorOnlyPolicy();
}

public final class MetricWorkBudget2D {
    long getMaximumEvaluations();
    long getMaximumSubdivisions();
    int getMaximumDepth();
}
```

Initial versioned G7B policy defaults:

```text
absolute tolerance = 1e-10 construction length unit
relative tolerance = 1e-9
maximum refinement depth = 22
maximum metric evaluations = 32768
maximum metric subdivisions = 16384
```

These values are implementation-policy defaults, not mathematical constants.
The three independent deterministic guards are
checked before new work. Exhaustion returns `LIMIT_NOT_ESTABLISHED`, never a
partial finite success. The complete work budget participates in policy
equality and every index key.

The effective threshold is `max(epsAbs, epsRel * scale)`, where scale is
world-coordinate, translation invariant and based on the current length
estimate, endpoint chord and any explicit provider characteristic scale.

## 6. Capability and integration

The G6 point evaluator should not be changed into a mandatory differential
interface. Add an optional capability:

```java
public interface LocusDifferentialEvaluator2D {
    LocusDifferentialEvaluation2D evaluateDifferential(
            String branchKey,
            double providerCanonicalParameter,
            LocusEvaluationSession2D session);
}
```

```java
public interface LocusMetricComponentStateBuilder2D {
    LocusMetricComponentState2D build(
            LocusDefinition2D definition,
            String branchKey,
            String resolvedValidComponentKey,
            LocusMetricPolicy2D policy,
            LocusMetricInstrumentation2D instrumentation);
}

public interface LocusMetricComponentEvaluator2D {
    LocusMetricContribution2D evaluateRouteSegment(
            LocusMetricComponentState2D state,
            LocusMetricRouteSegment2D segment,
            LocusMetricPolicy2D policy,
            LocusMetricInstrumentation2D instrumentation);

    LocusMetricContribution2D evaluateCompleteComponent(
            LocusMetricComponentState2D state,
            LocusMetricPolicy2D policy,
            LocusMetricInstrumentation2D instrumentation);
}
```

Capability order is:

1. analytic/closed form;
2. differential quadrature;
3. evaluator-only adaptive metric;
4. unsupported.

The G7A candidate is a small GeoCeDG-owned deterministic adaptive integrator
with per-call state, explicit counters, typed ceiling and error estimate. The
existing static adaptive Gauss helper is not a semantic result API.

The builder always targets the complete valid component. A/B endpoints never
enter its key. The evaluator derives a route-specific contribution after state
lookup, while total evaluation uses the complete component extent. One state
can produce many different contributions.

Evaluator-only refinement agreement never creates a certified bound. Without
explicit assumptions it returns `FLOATING_POINT_UNCERTIFIED` or
`UNSUPPORTED`.

## 7. Contributions and aggregation

```java
public final class LocusMetricComponentState2D {
    String getLocusIdentity();
    long getSemanticRevision();
    String getBranchKey();
    String getResolvedValidComponentKey();
    MetricComponentPartition2D getAdaptivePartition();
    MetricArcCoordinateEvidence2D getCumulativeArcCoordinateEvidence();
    MetricCapabilityMetadata2D getCapabilityMetadata();
    MetricErrorEvidence2D getComponentErrorEvidence();
}

public final class LocusMetricContribution2D {
    String getBranchKey();
    String getResolvedValidComponentKey();
    MetricValue2D getMetricValue();
    MetricComputationStatus getComputationStatus();
    MetricRectifiability getRectifiability();
    MetricErrorEvidence2D getErrorEvidence();
    MetricProvenance2D getProvenance();
    List<MetricDiagnostic2D> getDiagnostics();
}
```

`LocusMetricComponentState2D` is defensive and deeply immutable. It is the only
value shared by the dedicated owner. It is neither a query result nor a route,
contribution or aggregate result.

```java
public final class LocusMetricAggregator2D {
    LocusMetricResult2D aggregate(
            LocusMetricQuery2D query,
            List<LocusMetricContribution2D> contributions);
}
```

The aggregator owns contribution ordering, constructive multiplicity, stable
compensated summation, infinity/absence precedence, aggregate error, weakest
guarantee, coverage, status and decomposition. It never integrates one
component.

## 8. Rich result

```java
public enum MetricValueKind {
    FINITE,
    POSITIVE_INFINITY,
    ABSENT
}

public sealed interface MetricValue2D permits FiniteMetricValue2D,
        PositiveInfinityMetricValue2D, AbsentMetricValue2D {
    MetricValueKind getKind();
    OptionalDouble getFiniteValue();
}

public final class FiniteMetricValue2D implements MetricValue2D {
    // The constructor accepts only a finite, non-negative value.
}

public final class PositiveInfinityMetricValue2D implements MetricValue2D {
    // No numeric sentinel is stored or returned.
}

public final class AbsentMetricValue2D implements MetricValue2D {
    // No numeric sentinel is stored or returned.
}

public enum MetricCoverage {
    COMPLETE,
    INCOMPLETE
}

public enum MetricComputationStatus {
    SUCCESS,
    INVALID_QUERY,
    UNSUPPORTED,
    NUMERICAL_FAILURE,
    LIMIT_NOT_ESTABLISHED
}

public enum MetricRectifiability {
    RECTIFIABLE,
    NON_RECTIFIABLE,
    UNDETERMINED
}

public enum TraversalOutcome {
    TARGET_REACHED,
    STOPPED_AT_BOUNDARY,
    WRAPPED_TO_START,
    TARGET_NOT_REACHABLE,
    DISCONTINUITY_ENCOUNTERED
}

public enum MetricErrorAmountKind {
    ESTABLISHED,
    NOT_ESTABLISHED,
    NOT_APPLICABLE
}

public sealed interface MetricErrorAmount2D permits
        EstablishedMetricErrorAmount2D,
        NotEstablishedMetricErrorAmount2D,
        NotApplicableMetricErrorAmount2D {
    MetricErrorAmountKind getKind();
}

public final class EstablishedMetricErrorAmount2D
        implements MetricErrorAmount2D {
    double getNonNegativeFiniteAmount();
}

public final class NotEstablishedMetricErrorAmount2D
        implements MetricErrorAmount2D {
}

public final class NotApplicableMetricErrorAmount2D
        implements MetricErrorAmount2D {
}

public enum MetricErrorEvidenceScope {
    COMPLETE_VALUE,
    REPORTED_PARTIAL_VALUE,
    NOT_APPLICABLE
}

public final class MetricErrorEvidence2D {
    Optional<LocusSemanticMetadata2D.NumericGuarantee> getNumericGuarantee();
    MetricErrorAmount2D getAbsoluteEvidence();
    MetricErrorAmount2D getRelativeEvidence();
    MetricErrorEvidenceScope getScope();
    String getMethod();
    List<String> getAssumptions();
    Optional<String> getCertificateMetadata();
}
```

G7 reuses the productive normative G6
`LocusSemanticMetadata2D.NumericGuarantee` directly and defines no second
guarantee enum. The optional is empty only when evidence is not applicable to
`PositiveInfinity` or `Absent`; it is not a fifth guarantee. Unknown error is
`NotEstablishedMetricErrorAmount2D`, never NaN, `-1`, magic zero or `null`.
The closed hierarchy makes a contradictory availability state plus optional
number unrepresentable. Evidence for a known subtotal of
an incomplete aggregate is scoped `REPORTED_PARTIAL_VALUE`.

```java
public final class LocusMetricResult2D {
    MetricValue2D getMetricValue();
    MetricCoverage getCoverage();
    MetricComputationStatus getComputationStatus();
    MetricRectifiability getRectifiability();
    Optional<TraversalOutcome> getTraversalOutcome();
    ConstructionFidelity getConstructionFidelity();
    EvaluatorMethod getEvaluatorMethod();
    MetricMethod getMetricMethod();
    RepresentationRole getRepresentationRole();
    MetricErrorEvidence2D getErrorEvidence();
    MetricUnit2D getUnit();
    MetricProvenance2D getProvenance();
    List<LocusMetricContribution2D> getContributions();
    List<MetricDiagnostic2D> getDiagnostics();
}
```

For a between-position result the optional contains the mandatory traversal
outcome required by that query contract. For a total result it is empty because
total length has no traversal. An equivalent total/between subtype separation
is valid; `null`, sentinels and `NOT_APPLICABLE` are not.

The value is deeply immutable. Defensive copies are required for every list.
A finite zero is a normal value. Neither result nor contribution exposes a
bare value/error double for non-applicable states. `DIVERGENT` is not an enum
member.

## 9. Index API

```java
public final class LocusMetricIndexKey2D {
    String getLocusIdentity();
    long getSemanticRevision();
    String getBranchKey();
    String getResolvedValidComponentKey();
    String getProviderEvaluatorCapabilityVersion();
    String getMetricAlgorithmVersion();
    String getMetricPolicyVersion();
    String getTolerancePolicyVersion();
    MetricMultiplicityPolicy getMultiplicityPolicy();
    ImproperLimitPolicy2D getImproperLimitPolicy();
    MetricWorkBudget2D getWorkBudget();
}
```

```java
public final class LocusMetricIndex2D {
    LocusMetricComponentState2D getOrBuildComponentState(
            LocusMetricIndexKey2D key,
            LocusMetricComponentStateBuilder2D builder);
    void invalidateRevision(String locusIdentity, long revision);
    void invalidateLocus(String locusIdentity);
    void clear();
    LocusMetricIndexStatistics2D statistics();
}
```

The accepted strategy is lazy per component and current revision. R1
changes ownership from per metric algorithm to one dedicated
shared owner per active source locus:

```java
public final class LocusMetricSharedOwner2D {
    LocusMetricComponentState2D getOrBuildComponentState(
            LocusMetricIndexKey2D key,
            LocusMetricComponentStateBuilder2D builder);
    void invalidateObsoleteRevision(long currentRevision);
    void clear();
    LocusMetricIndexStatistics2D statistics();
}

public interface LocusMetricOwnerLease2D extends AutoCloseable {
    LocusMetricSharedOwner2D getOwner();
    @Override void close();
}
```

`GeoLocusV2` supplies an internal acquire seam. Each `AlgoLocusMetricV2`
holds one lease and closes it during normal removal. The last lease releases
all entries; locus revision publication, undefined and removal also clear the
owner. A copy never inherits it. The service stores no Algo, result Geo or
dependency edge and never calls between algorithms.

The provisional capacity is 64 component entries per active locus owner—not
64 per metric output—with deterministic insertion-order eviction. Entries and
arc-coordinate arrays are immutable. Build into local state, publish only
after success, and always clear active build state in `finally`. There is no
Construction-wide or global/static repository, background mutation,
concurrent quadrature or obsolete revision history.

The shared owner exposes no method returning a route-specific contribution.
Only immutable `LocusMetricComponentState2D` is shared. Routes, query results,
contributions and aggregate results remain owned by the requesting algorithm.
The hard budget is one component-state build per complete compatible key until
eviction or invalidation.

`REFERENCE_NO_INDEX_REUSE` remains a selectable test mode and compares rich
values, statuses, guarantees, evidence, contributions and diagnostics against
the shared path.

## 10. Rich Geo and normal DAG

```java
public final class GeoLocusMetricResult extends GeoElement {
    LocusMetricResult2D getMetricResult();
    long getSourceSemanticRevision();
    boolean isScalarAdmissible();
    void beginMetricRevision(long revision);
    void publishMetricResult(long revision, LocusMetricResult2D result);
    void publishMetricFailure(long revision, LocusMetricResult2D failure);
}
```

Candidate GeoClass:

```java
GeoClass.LOCUS_METRIC_RESULT
```

`isDefined()` means a current immutable rich snapshot is published. It does not
mean `getDouble()` is available; the class does not implement `NumberValue` or
`GeoNumberValue`. The Geo is non-drawable, Algebra-visible in the laboratory,
non-editable and nonpersistent.

Publication follows P1. `beginMetricRevision(r+1)` makes the revision `r`
payload non-current before new downstream consumption. Success publishes one
coherent immutable result; a handled failure publishes one coherent `Absent`
rich failure snapshot for `r+1`. A failed component build publishes no index
entry, but never leaves the old success current. There is no old-value/new-
status hybrid and the scalar adapter is undefined.

For the G7B minimum, `copy`, `copyInternal` and `set` must produce an
unpublished target requiring recomputation. They must not copy revision,
binding, index or a partial current result. List/sequence copy remains
unsupported unless this guarantee can be upheld.

```java
public final class AlgoLocusMetricV2 extends AlgoElement {
    GeoLocusMetricResult getResult();
}
```

Input/output registration includes the source `GeoLocusV2` and every query
Geo dependency. `compute()` begins the current revision, resolves bindings and
route/total decomposition, obtains immutable contributions from the shared
owner, aggregates and atomically publishes success or P1 failure. The Algo
remains a normal direct DAG dependent of the locus. Active-build cleanup is in
`finally`; its owner lease closes during normal removal.

## 11. Explicit scalar adapter

The accepted numeric strategy is C:

```java
public final class AlgoLocusMetricScalarAdapter extends AlgoElement {
    GeoLocusMetricResult getRichInput();
    GeoNumeric getScalarOutput();
}
```

This adapter is opt-in and derived. It publishes a finite scalar only when the
rich result is admissible; otherwise it sets its output undefined. It never
owns metric integration, result metadata or the index. There is no automatic
companion numeric and no public command in G7B.

Default admissible states:

- finite complete success;
- between-position target reached;
- valid zero;
- explicitly requested valid WRAP;
- at least an estimated error guarantee.

In G6 vocabulary, `EXACT_ARITHMETIC`, `CERTIFIED_ERROR_BOUND` and
`ESTIMATED_ERROR` are the candidate admissible guarantees.

Default inadmissible states:

- STOP partial;
- stale position, branch mismatch or discontinuity;
- incomplete aggregate;
- unsupported, numerical failure or limit not established;
- absent value;
- positive infinity;
- floating-point-uncertified result.

## 12. Total and between-position examples

```java
BetweenPositionsMetricQuery query = BetweenPositionsMetricQuery.builder()
        .start(bindingA)
        .target(bindingB)
        .direction(TraversalDirection.FORWARD)
        .boundaryPolicy(OpenBoundaryPolicy.STRICT)
        .samePositionPolicy(SamePositionPolicy.ZERO_LENGTH)
        .policy(metricPolicy)
        .build();
```

```java
TotalLocusMetricQuery total = new TotalLocusMetricQuery(
        locusIdentity, semanticRevision, metricPolicy);
```

The second query cannot be constructed with endpoints or a direction.

## 13. Public and compatibility boundary

G7B candidate exposure is limited to internal Java API,
`GeoLocusMetricResult` and the opt-in developer laboratory. It adds no
`LocusLength` command, changes no `Length`/`Perimeter`, implements no Path,
serializes no XML, registers no factory, adds no 3D behavior and starts no G8
or G9 work.

Legacy `GeoLocus`, `AlgoLengthLocus`, `AlgoPerimeterLocus`, `myPointList`,
`listLength`, `listLength12` and `postLocus` remain unchanged. Cache ON/OFF must
be semantically equal.

## 14. Author disposition and execution boundary

The author accepted the
[G7A decision table](../validation/g7a_locus_v2_metric_characterization_report.md),
the
[G7A-R1 decision table](../validation/g7a_r1_locus_v2_metric_refinement_report.md),
the normative metric specification and ADR 0007. This document does not execute
the separately versioned G7B prompt.

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED
G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0007 = ACCEPTED
G7B = AUTHORIZED / NOT STARTED
G8 = NOT STARTED
```

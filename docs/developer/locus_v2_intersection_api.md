# Locus V2 internal 2D intersection API candidate

| Field | Value |
|---|---|
| Status | **AUTHOR-APPROVED G8B CANDIDATE — NOT IMPLEMENTED / INTERNAL** |
| Evidence | [`g8a_locus_v2_intersection_characterization_report.md`](../validation/g8a_locus_v2_intersection_characterization_report.md) |
| G8A | **PASS — AUTHOR APPROVED** |
| G8B | **AUTHORIZED / NOT STARTED** |
| Public API | None |
| Date | 2026-08-14 |

This is the author-approved implementation candidate for a separately executed
G8B. It is not productive API and exposes no public surface. The normative G8
specification and Accepted ADR 0008 govern any implementation; exact Java
spelling may adapt to repository conventions without weakening those roles.

## 1. Package and dependency boundary

Authorized candidate semantic types belong under:

```text
org.geocedg.common.kernel.locus.intersection
```

Normal-DAG publication would use:

```text
org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2
org.geocedg.common.kernel.geos.GeoLocusIntersectionResult
```

The only reusable computation service is the productive G6 semantic evaluator
and bounded `LocusEvaluationSession2D`. The G7 metric owner, metric index,
metric component state and render cache are not dependencies.

## 2. Closed query and policy

```java
public final class LocusIntersectionQuery2D {
    String getSourcePairIdentity();
    String getLocusIdentity();
    long getLocusSemanticRevision();
    String getTargetIdentity();
    long getTargetUpdateStamp();
    LocusIntersectionPolicy2D getPolicy();
}

public final class LocusIntersectionPolicy2D {
    String getPolicyVersion();
    IntersectionParameterTolerance2D getRootParameterTolerance();
    IntersectionResidualTolerance2D getResidualTolerance();
    IntersectionTangencyTolerance2D getTangencyTolerance();
    IntersectionParameterTolerance2D getDeduplicationTolerance();
    IntersectionParameterTolerance2D getContinuationTolerance();
    IntersectionCoordinateTolerance2D getCoordinateVerificationTolerance();
    LocusIntersectionWorkBudget2D getWorkBudget();
}

public final class LocusIntersectionWorkBudget2D {
    long getMaximumSemanticEvaluations();
    long getMaximumSemanticDerivativeEvaluations();
    long getMaximumTargetEvaluations();
    int getMaximumCandidateIntervals();
    int getMaximumIsolationSubdivisions();
    int getMaximumIsolationDepth();
    int getMaximumRefinementIterationsPerCandidate();
    int getMaximumResidualVerifications();
    int getMaximumCandidateCount();
    int getMaximumContinuationComparisons();
    int getMaximumPublishedFiniteSolutions();
    int getMaximumRetainedIntersectionIndexEntries();
    int getMaximumRetainedTopologyEpochs();
}

public enum IntersectionResidualQuantityKind {
    MODEL_COORDINATE_DISTANCE,
    TARGET_FAMILY_SPECIFIC
}

public final class IntersectionParameterTolerance2D {
    double getValue();
    String getProviderId();
    String getProviderVersion();
    String getParameterUnitsOrNormalization();
}

public final class IntersectionResidualTolerance2D {
    IntersectionResidualQuantityKind getQuantityKind();
    String getUnits();
    double getAbsoluteTolerance();
    double getRelativeTolerance();
    String getCharacteristicScalePolicy();
}

public final class IntersectionTangencyTolerance2D {
    String getNormalizedContactIndicatorId();
    String getUnitsOrNormalization();
    double getThreshold();
}

public final class IntersectionCoordinateTolerance2D {
    double getModelCoordinateTolerance();
}
```

Every value is finite, positive and participates in policy equality together
with its quantity descriptor, units/normalization, provider/adapter
applicability and version. The approved implementation policy is
`g8b-initial-normalized/v1`, derived from `g8a-measured-candidate/v1`.

```text
rootParameterTolerance              = 1e-12
absoluteResidualTolerance           = 2e-12
relativeResidualTolerance           = 2e-12
tangencyThreshold                   = 1e-10
deduplicationParameterTolerance     = 4e-12
continuationParameterTolerance      = 1e-8
coordinateVerificationTolerance     = 4e-12
```

The root, deduplication and continuation quantities are in declared provider
semantic-parameter units, never universal Euclidean distance. Coordinate
verification is model-coordinate evidence and never identity. Residual and
tangency quantities are accepted only through the typed normalization contract
below; an adapter/provider uses a validated normalized equivalent when its
quantity differs from the characterized one.

The initial provisionally approved work values are:

```text
maximumSemanticEvaluations              = 32768
maximumSemanticDerivativeEvaluations    = 16384
maximumTargetEvaluations                = 32768
maximumCandidateIntervals               = 8192
maximumIsolationSubdivisions             = 8192
maximumIsolationDepth                    = 40
maximumRefinementIterationsPerCandidate  = 80
maximumResidualVerifications             = 1024
maximumCandidateCount                    = 512
maximumContinuationComparisons           = 4096
maximumPublishedFiniteSolutions          = 256
maximumRetainedTopologyEpochs             = 2
maximumRetainedIntersectionIndexEntries  = 0
```

These are versioned implementation defaults with G8A provenance, not
mathematical constants. Wall clock is informational.

## 3. Target authority adapters

```java
public enum IntersectionTargetFamily2D {
    LINE,
    SEGMENT,
    RAY,
    CIRCLE
}

public interface LocusIntersectionTarget2D {
    IntersectionTargetFamily2D getFamily();
    String getTargetIdentity();
    long getTargetUpdateStamp();
    IntersectionResidualContract2D getResidualContract();
    TargetResidual2D evaluateResidual(LocusPoint2D point);
    TargetMembership2D evaluateMembership(
            LocusPoint2D point,
            double coordinateVerificationTolerance);
    TargetContactEvidence2D evaluateContact(
            LocusPoint2D point,
            LocusDifferentialEvaluation2D differential);
}

public final class IntersectionResidualContract2D {
    String getAdapterVersion();
    IntersectionResidualQuantityKind getQuantityKind();
    String getUnits();
    String getNormalizationProvenance();
    String getCharacteristicScalePolicy();
}

public final class TargetResidual2D {
    double getRawResidual();
    double getNormalizationScale();
    double getNormalizedResidual();
    IntersectionResidualContract2D getContract();
}
```

`TargetResidual2D` carries raw residual, normalization scale, normalized
residual, quantity kind, units and provenance explicitly. `TargetMembership2D` is closed (`MEMBER`, `NOT_MEMBER`,
`NOT_ESTABLISHED`) with diagnostics. `TargetContactEvidence2D` distinguishes
established normalized-contact evidence from absence/unsupported evidence.

The preferred residual kind has a common model-coordinate distance meaning.
If a future target cannot provide one correctly, it must declare a
family-specific typed quantity and matching tolerance; incompatible quantities
cannot share a raw epsilon. Algebraically rescaling an equation by nonzero `c`
must not change the physical decision.

The minimum adapters snapshot actual target authority:

- line: homogeneous `GeoLine` coefficients normalized to signed perpendicular
  model-coordinate distance;
- segment/ray: the same support equation plus their native limited-path
  membership;
- circle: the `GeoConicND` matrix and verified circle type, exposed as signed
  radial-distance-equivalent residual.

No adapter converts an arbitrary object to an invented implicit curve. Full
conics, functions, implicit curves and locus–locus are absent from the G8B
minimum candidate.

For a regular source and model-distance residual,
`TargetContactEvidence2D` should use the target-normal/source-tangent
directional factor, equivalently `d residual / d source arc length`, as the
normalized first-order indicator. It must record normalization and capability
provenance. A raw `dh/dt` from an arbitrarily scaled equation or parameter is
inadmissible; singular/unsupported cases remain classification-undetermined
unless analytic or certified evidence establishes contact.

## 4. Result axes

```java
public enum IntersectionComputationStatus {
    SUCCESS,
    INVALID_INPUT,
    UNSUPPORTED,
    NUMERICAL_FAILURE,
    WORK_LIMIT_REACHED
}

public enum IntersectionCompleteness {
    COMPLETE,
    INCOMPLETE,
    NOT_ESTABLISHED
}

public enum IntersectionGeometryKind {
    EMPTY,
    FINITE,
    OVERLAP,
    INFINITELY_MANY,
    UNSUPPORTED_OVERLAP,
    UNRESOLVED
}

public enum IntersectionCurrentness {
    CURRENT,
    NON_CURRENT
}

public enum IntersectionSupportLevel {
    EXACT_CAPABILITY,
    CERTIFIED,
    VERIFIED_UNCERTIFIED,
    UNSUPPORTED
}
```

The result reuses
`LocusSemanticMetadata2D.NumericGuarantee`; it defines no duplicate numeric
guarantee enum. Exact algebraic capability provenance does not make evaluated
binary coordinates exact.

```java
public enum IntersectionCompletenessMethod {
    ANALYTIC_ROOT_ENUMERATION,
    CERTIFIED_DOMAIN_ISOLATION,
    CERTIFIED_DOMAIN_EXCLUSION,
    CONSERVATIVE_COVERAGE_PROOF,
    INCOMPLETE_CANDIDATE_COVERAGE,
    NOT_ESTABLISHED
}

public final class IntersectionCompletenessEvidence2D {
    IntersectionCompleteness getCompleteness();
    IntersectionCompletenessMethod getMethod();
    int getVerifiedRootCount();
    List<IntersectionDiagnostic2D> getDiagnostics();
}
```

`EMPTY` is constructible only with `COMPLETE`. A finite list of verified roots
does not imply completeness.

## 5. Durable identity and revision evidence

```java
public enum IntersectionIdentityStatus {
    CONTINUATION_ESTABLISHED,
    NEW_TOPOLOGICAL_SOLUTION,
    AMBIGUOUS_CONTINUATION,
    IDENTITY_DISCONTINUITY,
    NOT_ESTABLISHED
}

public final class IntersectionRootIdentity2D {
    String getRootToken();
    String getSourcePairIdentity();
    String getConstructiveIntersectionLineage();
    String getEstablishedBranchLineage();
    String getTopologyContext();
    IntersectionIdentityStatus getIdentityStatus();
}

public final class IntersectionRootRevisionEvidence2D {
    long getLocusSemanticRevision();
    long getTargetUpdateStamp();
    String getBranchSnapshotKey();
    String getResolvedValidComponentKey();
    double getSemanticParameter();
    OptionalDouble getLiftedPeriodicParameter();
    IntersectionParameterInterval2D getIsolatingInterval();
    TargetResidual2D getResidualEvidence();
    IntersectionSolverMethod getSolverMethod();
    NumericGuarantee getNumericGuarantee();
}
```

The first value is the accepted durable/continuation layer. The second is
revision-scoped numerical and localization evidence. In particular, neither a
semantic parameter nor an isolating interval is the fundamental root identity.

```java
public enum IntersectionLineageEventKind {
    UNCHANGED,
    APPEARED,
    DISAPPEARED,
    MERGE_CANDIDATE,
    SPLIT_CANDIDATE,
    AMBIGUOUS_EVENT
}

public final class IntersectionRootLineage2D {
    IntersectionLineageEventKind getEventKind();
    List<String> getCandidateParentTokens();
    List<String> getCandidateChildTokens();
    boolean isContinuationEstablished();
}
```

Merge/split parent-child sets are event evidence, not universal inheritance.
Symmetric cases use new tokens and explicit ambiguity.

## 6. Contact, multiplicity and finite solution

```java
public enum IntersectionContactClass {
    TRANSVERSE_ESTABLISHED,
    TANGENT_ESTABLISHED,
    CONTACT_UNDETERMINED
}

public enum IntersectionMultiplicityStatus {
    ESTABLISHED,
    NOT_ESTABLISHED
}

public enum IntersectionDomainLocation {
    INTERIOR,
    INCLUDED_ENDPOINT,
    PERIODIC_SEAM,
    ISOLATED_COMPONENT
}

public final class IntersectionClassification2D {
    IntersectionContactClass getContactClass();
    IntersectionMultiplicityStatus getMultiplicityStatus();
    OptionalInt getEstablishedMultiplicity();
    IntersectionDomainLocation getDomainLocation();
}

public final class LocusIntersectionSolution2D {
    IntersectionRootIdentity2D getIdentity();
    IntersectionRootRevisionEvidence2D getRevisionEvidence();
    LocusPoint2D getEvaluatedPoint();
    IntersectionClassification2D getClassification();
    IntersectionRootLineage2D getLineage();
    List<IntersectionDiagnostic2D> getDiagnostics();
}
```

Unknown multiplicity has no integer sentinel. Equal coordinates do not merge
different semantic preimages.

## 7. Immutable rich result

```java
public final class LocusIntersectionResult2D {
    IntersectionSourceBinding2D getSourceBinding();
    IntersectionComputationStatus getComputationStatus();
    IntersectionCompletenessEvidence2D getCompletenessEvidence();
    IntersectionGeometryKind getGeometryKind();
    IntersectionCurrentness getCurrentness();
    IntersectionSupportLevel getSupportLevel();
    NumericGuarantee getNumericGuarantee();
    List<LocusIntersectionSolution2D> getFiniteSolutions();
    List<IntersectionOverlapEvidence2D> getOverlapEvidence();
    LocusIntersectionInstrumentationSnapshot2D getWork();
    List<IntersectionDiagnostic2D> getDiagnostics();
    boolean isFinitePointProjectionAdmissible();
}
```

The value is deeply immutable. `OVERLAP` and `INFINITELY_MANY` carry typed
component evidence, never arbitrary point samples. Point projection is
admissible only for current, successful, complete finite results.

## 8. Query-local solver pipeline

```java
public interface LocusIntersectionCapability2D {
    LocusIntersectionResult2D solve(
            LocusIntersectionQuery2D query,
            LocusDefinition2D definition,
            LocusIntersectionTarget2D target,
            LocusEvaluationSession2D evaluationSession,
            LocusIntersectionInstrumentation2D instrumentation);
}

public final class LocusIntersectionSolver2D {
    LocusIntersectionResult2D intersect(
            LocusIntersectionQuery2D query,
            LocusDefinition2D definition,
            LocusIntersectionTarget2D target);
}
```

The solver owns one bounded query-local context. The pipeline is:

```text
target-specific analytic/certified capability where available
    -> exhaustive component isolation or explicit non-completeness
    -> semantic-parameter refinement
    -> independent semantic/target residual and membership verification
    -> semantic-parameter/seam deduplication
    -> immutable result publication
```

Evaluator-only sampling and any spatial bounds may generate candidates only.
They cannot certify absence, completeness, tangency or identity by themselves.
No shared intersection owner or cache is in the G8B candidate.

## 9. Continuation service

```java
public interface IntersectionParameterContinuation2D {
    ParameterContinuationEvidence2D mapPreviousToCurrent(
            String establishedBranchLineage,
            double previousCanonicalParameter,
            LocusDefinition2D previousDefinition,
            LocusDefinition2D currentDefinition);
}

public final class LocusIntersectionContinuation2D {
    IntersectionContinuationResult2D continueRoots(
            LocusIntersectionResult2D previous,
            LocusIntersectionResult2D current,
            IntersectionParameterContinuation2D parameterMap,
            LocusIntersectionPolicy2D policy,
            LocusIntersectionInstrumentation2D instrumentation);
}
```

Only the previous and current topology epochs are retained. A token continues
only when source pair, constructive lineage, established branch lineage and an
explicit semantic continuation relation select exactly one admissible root.
Coordinates are not matching keys.

## 10. Rich Geo and normal DAG

```java
public final class GeoLocusIntersectionResult extends GeoElement {
    LocusIntersectionResult2D getIntersectionResult();
    IntersectionSourceBinding2D getCurrentSourceBinding();
    void beginIntersectionRevision(IntersectionSourceBinding2D binding);
    void publishIntersectionResult(
            IntersectionSourceBinding2D binding,
            LocusIntersectionResult2D result);
    boolean isFinitePointProjectionAdmissible();
}

public final class AlgoLocusIntersectionV2 extends AlgoElement {
    GeoLocusIntersectionResult getResult();
}
```

Authorized classification if required by the rich Geo:

```java
GeoClass.LOCUS_INTERSECTION_RESULT
```

The Geo is nonnumeric, non-editable, non-drawable, internal and nonpersistent.
`copy`, `copyInternal` and `set` clear revision-bound payload and require
recomputation. `beginIntersectionRevision` makes the previous snapshot
non-current before any new work. Publication is atomic on success, supported
absence, overlap, unsupported input, exception or budget exhaustion.

## 11. Required identified-point consumer

To satisfy the first-class downstream CeDG requirement, G8B must implement one
separate internal, token-selected consumer:

```java
public final class AlgoLocusIntersectionPointV2 extends AlgoElement {
    GeoLocusIntersectionResult getRichInput();
    String getSelectedRootToken();
    GeoPoint getPoint();
}
```

It produces one ordinary point only while the selected token is current in a
complete finite result. Ambiguity, incomplete/not-established completeness,
overlap, failure or token termination makes it undefined. It must not retarget
to a different solution and may recover only when the same token is current
again under the approved lifecycle contract. It owns no solver, identity or
cache. A variable-size public point array is not authorized.

## 12. Exact candidate productive edit set

The smallest authorized G8B candidate set is:

1. new immutable intersection values, policy, instrumentation, target adapters,
   query-local solver and continuation types under
   `.../kernel/locus/intersection/`;
2. `AlgoLocusIntersectionV2` and `GeoLocusIntersectionResult`;
3. required `AlgoLocusIntersectionPointV2`-equivalent identified-point
   consumer;
4. one append-only `GeoClass.LOCUS_INTERSECTION_RESULT` addition plus required
   exhaustive-type tests if the rich Geo requires the classification;
5. focused productive unit/lifecycle/topology tests; and
6. specification, API, modified-upstream ledger and validation evidence.

No edit is proposed to `GeoLocusV2`, `GeoLocus`, `CmdIntersect`,
`AlgoDispatcher`, Classic `AlgoIntersect*`, `Path`, `GeoFactory`, XML,
rendering, export, 3D, Python or G9 sources. A future implementation must stop
if an additional public or persistence dependency becomes necessary.

## 13. Current disposition

```text
G8A = PASS — AUTHOR APPROVED
G8 SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0008 = ACCEPTED
G8B = AUTHORIZED / NOT STARTED
G8 PRODUCTIVE IMPLEMENTATION = NOT STARTED
G9 = NOT STARTED
```

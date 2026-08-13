/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/**
 * Test-private candidate values used only by the G7A characterization.
 *
 * <p>This file is deliberately outside {@code src/main}. Its names and
 * behavior are evidence for author review, not a productive G7 API.</p>
 */
final class G7AMetricSemanticModel {

	private G7AMetricSemanticModel() {
		// Utility class.
	}

	enum Direction {
		FORWARD,
		REVERSE
	}

	enum BoundaryPolicy {
		STOP_AT_END,
		WRAP_TO_START,
		STRICT
	}

	enum SamePositionPolicy {
		ZERO_LENGTH,
		FULL_CYCLE
	}

	enum BindingStatus {
		VALID,
		POSITION_STALE,
		POSITION_OUTSIDE_DOMAIN,
		BRANCH_MISSING
	}

	enum RouteStatus {
		RESOLVED,
		POSITION_STALE,
		DIFFERENT_LOCUS,
		DIFFERENT_BRANCH,
		DISCONTINUITY_ENCOUNTERED,
		TARGET_NOT_REACHABLE,
		INVALID_QUERY
	}

	enum TraversalOutcome {
		TARGET_REACHED,
		STOPPED_AT_BOUNDARY,
		WRAPPED_TO_START,
		TARGET_NOT_REACHABLE,
		DISCONTINUITY_ENCOUNTERED
	}

	enum SegmentRole {
		DIRECT,
		TO_GLOBAL_BOUNDARY,
		FROM_GLOBAL_BOUNDARY,
		PERIODIC_CYCLE
	}

	enum MetricValueKind {
		FINITE,
		POSITIVE_INFINITY,
		ABSENT
	}

	sealed interface MetricValue2D permits FiniteMetricValue2D,
			PositiveInfinityMetricValue2D, AbsentMetricValue2D {
		MetricValueKind kind();

		default OptionalDouble finiteValue() {
			return OptionalDouble.empty();
		}
	}

	record FiniteMetricValue2D(double value) implements MetricValue2D {
		FiniteMetricValue2D {
			if (!Double.isFinite(value) || value < 0) {
				throw new IllegalArgumentException(
						"Finite metric values must be non-negative");
			}
		}

		@Override
		public MetricValueKind kind() {
			return MetricValueKind.FINITE;
		}

		@Override
		public OptionalDouble finiteValue() {
			return OptionalDouble.of(value);
		}
	}

	record PositiveInfinityMetricValue2D() implements MetricValue2D {
		@Override
		public MetricValueKind kind() {
			return MetricValueKind.POSITIVE_INFINITY;
		}
	}

	record AbsentMetricValue2D() implements MetricValue2D {
		@Override
		public MetricValueKind kind() {
			return MetricValueKind.ABSENT;
		}
	}

	enum MetricErrorAmountState {
		ESTABLISHED,
		NOT_ESTABLISHED,
		NOT_APPLICABLE
	}

	enum MetricErrorEvidenceScope {
		COMPLETE_VALUE,
		REPORTED_PARTIAL_VALUE,
		NOT_APPLICABLE
	}

	sealed interface MetricErrorAmount2D permits EstablishedMetricErrorAmount2D,
			NotEstablishedMetricErrorAmount2D,
			NotApplicableMetricErrorAmount2D {
		MetricErrorAmountState state();

		OptionalDouble amount();

		static MetricErrorAmount2D established(double amount) {
			return new EstablishedMetricErrorAmount2D(amount);
		}

		static MetricErrorAmount2D notEstablished() {
			return new NotEstablishedMetricErrorAmount2D();
		}

		static MetricErrorAmount2D notApplicable() {
			return new NotApplicableMetricErrorAmount2D();
		}
	}

	record EstablishedMetricErrorAmount2D(double value)
			implements MetricErrorAmount2D {
		EstablishedMetricErrorAmount2D {
			if (!Double.isFinite(value) || value < 0) {
				throw new IllegalArgumentException(
						"Established error must be finite and non-negative");
			}
		}

		@Override
		public MetricErrorAmountState state() {
			return MetricErrorAmountState.ESTABLISHED;
		}

		@Override
		public OptionalDouble amount() {
			return OptionalDouble.of(value);
		}
	}

	record NotEstablishedMetricErrorAmount2D()
			implements MetricErrorAmount2D {
		@Override
		public MetricErrorAmountState state() {
			return MetricErrorAmountState.NOT_ESTABLISHED;
		}

		@Override
		public OptionalDouble amount() {
			return OptionalDouble.empty();
		}
	}

	record NotApplicableMetricErrorAmount2D()
			implements MetricErrorAmount2D {
		@Override
		public MetricErrorAmountState state() {
			return MetricErrorAmountState.NOT_APPLICABLE;
		}

		@Override
		public OptionalDouble amount() {
			return OptionalDouble.empty();
		}
	}

	record MetricErrorEvidence2D(Optional<NumericGuarantee> numericGuarantee,
			MetricErrorAmount2D absoluteEvidence,
			MetricErrorAmount2D relativeEvidence, MetricErrorEvidenceScope scope,
			String method,
			List<String> assumptions, Optional<String> certificateMetadata) {
		MetricErrorEvidence2D {
			Objects.requireNonNull(numericGuarantee);
			Objects.requireNonNull(absoluteEvidence);
			Objects.requireNonNull(relativeEvidence);
			Objects.requireNonNull(scope);
			Objects.requireNonNull(method);
			Objects.requireNonNull(certificateMetadata);
			assumptions = List.copyOf(assumptions);
			if (numericGuarantee.isEmpty()
					&& (absoluteEvidence.state()
							!= MetricErrorAmountState.NOT_APPLICABLE
							|| relativeEvidence.state()
									!= MetricErrorAmountState.NOT_APPLICABLE)) {
				throw new IllegalArgumentException(
						"A non-numeric metric value has no applicable error evidence");
			}
			if (numericGuarantee.isEmpty()
					!= (scope == MetricErrorEvidenceScope.NOT_APPLICABLE)) {
				throw new IllegalArgumentException(
						"Evidence scope must agree with numeric applicability");
			}
		}

		static MetricErrorEvidence2D exact(boolean relativeApplicable,
				String method) {
			return new MetricErrorEvidence2D(
					Optional.of(NumericGuarantee.EXACT_ARITHMETIC),
					MetricErrorAmount2D.established(0),
					relativeApplicable ? MetricErrorAmount2D.established(0)
							: MetricErrorAmount2D.notApplicable(),
					MetricErrorEvidenceScope.COMPLETE_VALUE, method, List.of(),
					Optional.of("exact arithmetic"));
		}

		static MetricErrorEvidence2D certified(double absolute,
				OptionalDouble relative, String method, String certificate) {
			return numeric(NumericGuarantee.CERTIFIED_ERROR_BOUND, absolute,
					relative, method, List.of(), Optional.of(certificate));
		}

		static MetricErrorEvidence2D estimated(double absolute,
				OptionalDouble relative, String method, List<String> assumptions) {
			return numeric(NumericGuarantee.ESTIMATED_ERROR, absolute, relative,
					method, assumptions, Optional.empty());
		}

		static MetricErrorEvidence2D uncertified(String method,
				List<String> assumptions) {
			return new MetricErrorEvidence2D(
					Optional.of(NumericGuarantee.FLOATING_POINT_UNCERTIFIED),
					MetricErrorAmount2D.notEstablished(),
					MetricErrorAmount2D.notEstablished(),
					MetricErrorEvidenceScope.COMPLETE_VALUE, method, assumptions,
					Optional.empty());
		}

		static MetricErrorEvidence2D notApplicable(String method) {
			return new MetricErrorEvidence2D(Optional.empty(),
					MetricErrorAmount2D.notApplicable(),
					MetricErrorAmount2D.notApplicable(),
					MetricErrorEvidenceScope.NOT_APPLICABLE, method, List.of(),
					Optional.empty());
		}

		MetricErrorEvidence2D asReportedPartialValue() {
			if (numericGuarantee.isEmpty()) {
				return this;
			}
			return new MetricErrorEvidence2D(numericGuarantee,
					absoluteEvidence, relativeEvidence,
					MetricErrorEvidenceScope.REPORTED_PARTIAL_VALUE, method,
					assumptions, certificateMetadata);
		}

		private static MetricErrorEvidence2D numeric(
				NumericGuarantee guarantee, double absolute,
				OptionalDouble relative, String method, List<String> assumptions,
				Optional<String> certificate) {
			return new MetricErrorEvidence2D(Optional.of(guarantee),
					MetricErrorAmount2D.established(absolute),
					relative.isPresent()
							? MetricErrorAmount2D.established(relative.getAsDouble())
							: MetricErrorAmount2D.notApplicable(),
					MetricErrorEvidenceScope.COMPLETE_VALUE, method, assumptions,
					certificate);
		}
	}

	enum MetricCoverage {
		COMPLETE,
		INCOMPLETE
	}

	enum MetricComputationStatus {
		SUCCESS,
		INVALID_QUERY,
		UNSUPPORTED,
		NUMERICAL_FAILURE,
		LIMIT_NOT_ESTABLISHED
	}

	enum MetricRectifiability {
		RECTIFIABLE,
		NON_RECTIFIABLE,
		UNDETERMINED
	}

	enum ConstructionFidelity {
		EXACT_CONSTRUCTION,
		SEMANTIC_NUMERICAL_EVALUATION,
		UNKNOWN
	}

	enum EvaluatorMethod {
		ANALYTIC,
		DIFFERENTIAL,
		POINT_EVALUATOR_ONLY,
		NONE
	}

	enum MetricMethod {
		CLOSED_FORM,
		ADAPTIVE_DIFFERENTIAL_QUADRATURE,
		ADAPTIVE_EVALUATOR_METRIC,
		IMPROPER_LIMIT,
		NONE
	}

	enum RepresentationRole {
		SEMANTIC_METRIC,
		DERIVED_ARC_COORDINATE,
		DIAGNOSTIC_PARTIAL_VALUE
	}

	record Point(double x, double y) {
		double distance(Point other) {
			return Math.hypot(x - other.x, y - other.y);
		}
	}

	record LocusSemanticPosition2D(String locusIdentity, String branchKey,
			String providerVersion, double providerCanonicalParameter) {
		LocusSemanticPosition2D {
			Objects.requireNonNull(locusIdentity);
			Objects.requireNonNull(branchKey);
			Objects.requireNonNull(providerVersion);
		}
	}

	record ValidComponent(String key, double start, double end,
			boolean startIncluded, boolean endIncluded) {
		ValidComponent {
			Objects.requireNonNull(key);
			if (!Double.isFinite(start) || !Double.isFinite(end) || start > end) {
				throw new IllegalArgumentException("Invalid finite component");
			}
		}

		boolean contains(double parameter) {
			boolean afterStart = parameter > start
					|| startIncluded && parameter == start;
			boolean beforeEnd = parameter < end
					|| endIncluded && parameter == end;
			return afterStart && beforeEnd;
		}
	}

	record BranchRevision(String locusIdentity, long semanticRevision,
			String branchKey, boolean periodic,
			List<ValidComponent> validComponents) {
		BranchRevision {
			Objects.requireNonNull(locusIdentity);
			Objects.requireNonNull(branchKey);
			validComponents = List.copyOf(validComponents);
			if (semanticRevision < 1) {
				throw new IllegalArgumentException("Revision must be positive");
			}
		}

		double globalStart() {
			return validComponents.get(0).start();
		}

		double globalEnd() {
			return validComponents.get(validComponents.size() - 1).end();
		}
	}

	record MetricPositionBinding2D(LocusSemanticPosition2D semanticPosition,
			long semanticRevision, String resolvedValidComponentKey,
			BindingStatus evaluationStatus, Point evaluatedPoint,
			List<String> diagnostics) {
		MetricPositionBinding2D {
			Objects.requireNonNull(semanticPosition);
			Objects.requireNonNull(evaluationStatus);
			diagnostics = List.copyOf(diagnostics);
		}

		boolean valid() {
			return evaluationStatus == BindingStatus.VALID;
		}
	}

	static MetricPositionBinding2D bind(LocusSemanticPosition2D position,
			BranchRevision revision) {
		if (!position.locusIdentity().equals(revision.locusIdentity())
				|| !position.branchKey().equals(revision.branchKey())) {
			return new MetricPositionBinding2D(position,
					revision.semanticRevision(), null, BindingStatus.BRANCH_MISSING,
					null, List.of("branch absent in requested revision"));
		}
		for (ValidComponent component : revision.validComponents()) {
			if (component.contains(position.providerCanonicalParameter())) {
				double parameter = position.providerCanonicalParameter();
				return new MetricPositionBinding2D(position,
						revision.semanticRevision(), component.key(),
						BindingStatus.VALID, new Point(parameter, parameter * parameter),
						List.of());
			}
		}
		return new MetricPositionBinding2D(position, revision.semanticRevision(),
				null, BindingStatus.POSITION_OUTSIDE_DOMAIN, null,
				List.of("canonical parameter is outside every valid component"));
	}

	static MetricPositionBinding2D useAtRevision(
			MetricPositionBinding2D binding, long currentRevision) {
		if (binding.semanticRevision() == currentRevision) {
			return binding;
		}
		return new MetricPositionBinding2D(binding.semanticPosition(),
				binding.semanticRevision(), binding.resolvedValidComponentKey(),
				BindingStatus.POSITION_STALE, binding.evaluatedPoint(),
				List.of("explicit rebind required; coordinate repair forbidden"));
	}

	record BetweenPositionsMetricQuery(MetricPositionBinding2D start,
			MetricPositionBinding2D target, Direction direction,
			BoundaryPolicy boundaryPolicy, SamePositionPolicy samePositionPolicy) {
		BetweenPositionsMetricQuery {
			Objects.requireNonNull(start);
			Objects.requireNonNull(target);
			Objects.requireNonNull(direction);
			Objects.requireNonNull(boundaryPolicy);
			Objects.requireNonNull(samePositionPolicy);
		}
	}

	record TotalLocusMetricQuery(String locusIdentity, long semanticRevision) {
		TotalLocusMetricQuery {
			Objects.requireNonNull(locusIdentity);
		}
	}

	record LocusMetricRouteSegment2D(String resolvedValidComponentKey,
			double fromParameter, double toParameter, Direction direction,
			SegmentRole role) {
		LocusMetricRouteSegment2D {
			Objects.requireNonNull(resolvedValidComponentKey);
			Objects.requireNonNull(direction);
			Objects.requireNonNull(role);
		}

		double straightFixtureLength() {
			return Math.abs(toParameter - fromParameter);
		}
	}

	record LocusMetricRoute2D(String locusIdentity, long semanticRevision,
			String branchKey, List<LocusMetricRouteSegment2D> orderedRouteSegments,
			Direction direction, BoundaryPolicy boundaryPolicy,
			boolean targetReached, boolean wrapped,
			boolean geometricallyConnected, RouteStatus routeStatus,
			TraversalOutcome traversalOutcome, List<String> diagnostics) {
		LocusMetricRoute2D {
			orderedRouteSegments = List.copyOf(orderedRouteSegments);
			diagnostics = List.copyOf(diagnostics);
		}
	}

	static final class LocusMetricRouteResolver2D {
		LocusMetricRoute2D resolve(BetweenPositionsMetricQuery query,
				BranchRevision branch) {
			MetricPositionBinding2D start = query.start();
			MetricPositionBinding2D target = query.target();
			LocusSemanticPosition2D startPosition = start.semanticPosition();
			LocusSemanticPosition2D targetPosition = target.semanticPosition();
			if (start.evaluationStatus() == BindingStatus.POSITION_STALE
					|| target.evaluationStatus() == BindingStatus.POSITION_STALE) {
				return failure(query, branch, RouteStatus.POSITION_STALE,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"stale binding requires explicit rebind");
			}
			if (!start.valid() || !target.valid()) {
				return failure(query, branch, RouteStatus.INVALID_QUERY,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"both positions must bind to valid domain components");
			}
			if (!startPosition.locusIdentity().equals(
					targetPosition.locusIdentity())) {
				return failure(query, branch, RouteStatus.DIFFERENT_LOCUS,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"positions belong to different loci");
			}
			if (!startPosition.branchKey().equals(targetPosition.branchKey())) {
				return failure(query, branch, RouteStatus.DIFFERENT_BRANCH,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"positions belong to different constructive branches");
			}
			if (start.semanticRevision() != branch.semanticRevision()
					|| target.semanticRevision() != branch.semanticRevision()) {
				return failure(query, branch, RouteStatus.POSITION_STALE,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"binding revision does not match the current branch revision");
			}
			boolean samePosition = startPosition.equals(targetPosition);
			if (samePosition) {
				return resolveSamePosition(query, branch);
			}
			if (!start.resolvedValidComponentKey().equals(
					target.resolvedValidComponentKey())) {
				return failure(query, branch,
						RouteStatus.DISCONTINUITY_ENCOUNTERED,
						TraversalOutcome.DISCONTINUITY_ENCOUNTERED,
						"no boundary policy may cross an internal invalid-domain gap");
			}
			ValidComponent component = component(branch,
					start.resolvedValidComponentKey());
			double from = startPosition.providerCanonicalParameter();
			double to = targetPosition.providerCanonicalParameter();
			boolean directlyReachable = query.direction() == Direction.FORWARD
					? to > from : to < from;
			if (directlyReachable) {
				return success(query, branch,
						List.of(segment(component, from, to, query.direction(),
								SegmentRole.DIRECT)), true, false, true,
						TraversalOutcome.TARGET_REACHED);
			}
			if (branch.periodic()) {
				return periodicAcrossSeam(query, branch, component, from, to,
						SegmentRole.DIRECT);
			}
			return resolveOpenBoundary(query, branch, component, from, to);
		}

		private LocusMetricRoute2D resolveSamePosition(
				BetweenPositionsMetricQuery query, BranchRevision branch) {
			if (query.samePositionPolicy() == SamePositionPolicy.ZERO_LENGTH) {
				return success(query, branch, List.of(), true, false, true,
						TraversalOutcome.TARGET_REACHED);
			}
			if (!branch.periodic()) {
				return failure(query, branch, RouteStatus.INVALID_QUERY,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"FULL_CYCLE requires approved periodic semantics");
			}
			ValidComponent component = component(branch,
					query.start().resolvedValidComponentKey());
			double parameter = query.start().semanticPosition()
					.providerCanonicalParameter();
			return periodicAcrossSeam(query, branch, component, parameter,
					parameter, SegmentRole.PERIODIC_CYCLE);
		}

		private LocusMetricRoute2D periodicAcrossSeam(
				BetweenPositionsMetricQuery query, BranchRevision branch,
				ValidComponent component, double from, double to,
				SegmentRole directRole) {
			List<LocusMetricRouteSegment2D> segments = new ArrayList<>();
			if (query.direction() == Direction.FORWARD) {
				addIfNonZero(segments, segment(component, from, component.end(),
						Direction.FORWARD, directRole));
				addIfNonZero(segments, segment(component, component.start(), to,
						Direction.FORWARD, directRole));
			} else {
				addIfNonZero(segments, segment(component, from, component.start(),
						Direction.REVERSE, directRole));
				addIfNonZero(segments, segment(component, component.end(), to,
						Direction.REVERSE, directRole));
			}
			if (segments.isEmpty()) {
				segments.add(segment(component, component.start(), component.end(),
						query.direction(), SegmentRole.PERIODIC_CYCLE));
			}
			return success(query, branch, segments, true, true, true,
					TraversalOutcome.TARGET_REACHED);
		}

		private LocusMetricRoute2D resolveOpenBoundary(
				BetweenPositionsMetricQuery query, BranchRevision branch,
				ValidComponent component, double from, double to) {
			if (query.boundaryPolicy() == BoundaryPolicy.STRICT) {
				return failure(query, branch, RouteStatus.TARGET_NOT_REACHABLE,
						TraversalOutcome.TARGET_NOT_REACHABLE,
						"STRICT rejects an unreachable target");
			}
			double boundary = query.direction() == Direction.FORWARD
					? component.end() : component.start();
			LocusMetricRouteSegment2D toBoundary = segment(component, from,
					boundary, query.direction(), SegmentRole.TO_GLOBAL_BOUNDARY);
			if (query.boundaryPolicy() == BoundaryPolicy.STOP_AT_END) {
				return success(query, branch, List.of(toBoundary), false, false,
						true, TraversalOutcome.STOPPED_AT_BOUNDARY);
			}
			boolean singleGlobalComponent = branch.validComponents().size() == 1
					&& component.start() == branch.globalStart()
					&& component.end() == branch.globalEnd();
			if (!singleGlobalComponent) {
				return failure(query, branch,
						RouteStatus.DISCONTINUITY_ENCOUNTERED,
						TraversalOutcome.DISCONTINUITY_ENCOUNTERED,
						"WRAP cannot cross an internal invalid-domain gap");
			}
			double restart = query.direction() == Direction.FORWARD
					? component.start() : component.end();
			LocusMetricRouteSegment2D fromBoundary = segment(component, restart,
					to, query.direction(), SegmentRole.FROM_GLOBAL_BOUNDARY);
			return success(query, branch, List.of(toBoundary, fromBoundary), true,
					true, false, TraversalOutcome.WRAPPED_TO_START);
		}

		private static LocusMetricRouteSegment2D segment(
				ValidComponent component, double from, double to,
				Direction direction, SegmentRole role) {
			return new LocusMetricRouteSegment2D(component.key(), from, to,
					direction, role);
		}

		private static void addIfNonZero(
				List<LocusMetricRouteSegment2D> segments,
				LocusMetricRouteSegment2D segment) {
			if (segment.fromParameter() != segment.toParameter()) {
				segments.add(segment);
			}
		}

		private static ValidComponent component(BranchRevision branch,
				String key) {
			return branch.validComponents().stream()
					.filter(candidate -> candidate.key().equals(key))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException(
							"binding component is absent from branch revision"));
		}

		private static LocusMetricRoute2D success(
				BetweenPositionsMetricQuery query, BranchRevision branch,
				List<LocusMetricRouteSegment2D> segments, boolean reached,
				boolean wrapped, boolean connected, TraversalOutcome outcome) {
			return new LocusMetricRoute2D(branch.locusIdentity(),
					branch.semanticRevision(), branch.branchKey(), segments,
					query.direction(), query.boundaryPolicy(), reached, wrapped,
					connected, RouteStatus.RESOLVED, outcome, List.of());
		}

		private static LocusMetricRoute2D failure(
				BetweenPositionsMetricQuery query, BranchRevision branch,
				RouteStatus status, TraversalOutcome outcome, String diagnostic) {
			return new LocusMetricRoute2D(branch.locusIdentity(),
					branch.semanticRevision(), branch.branchKey(), List.of(),
					query.direction(), query.boundaryPolicy(), false, false, false,
					status, outcome, List.of(diagnostic));
		}
	}

	record MetricContribution2D(String branchKey, String componentKey,
			MetricValue2D metricValue,
			MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			MetricErrorEvidence2D errorEvidence, String provenance,
			List<String> diagnostics) {
		MetricContribution2D {
			Objects.requireNonNull(branchKey);
			Objects.requireNonNull(componentKey);
			Objects.requireNonNull(metricValue);
			Objects.requireNonNull(computationStatus);
			Objects.requireNonNull(rectifiability);
			Objects.requireNonNull(errorEvidence);
			Objects.requireNonNull(provenance);
			diagnostics = List.copyOf(diagnostics);
		}

		MetricValueKind valueKind() {
			return metricValue.kind();
		}

		OptionalDouble finiteValue() {
			return metricValue.finiteValue();
		}

		Optional<NumericGuarantee> numericGuarantee() {
			return errorEvidence.numericGuarantee();
		}
	}

	record LocusMetricResult2D(MetricValue2D metricValue,
			MetricCoverage coverage, MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			Optional<TraversalOutcome> traversalOutcome,
			ConstructionFidelity constructionFidelity,
			EvaluatorMethod evaluatorMethod, MetricMethod metricMethod,
			RepresentationRole representationRole,
			MetricErrorEvidence2D errorEvidence, String units,
			String provenance, List<MetricContribution2D> contributions,
			List<String> diagnostics) {
		LocusMetricResult2D {
			Objects.requireNonNull(metricValue);
			Objects.requireNonNull(coverage);
			Objects.requireNonNull(computationStatus);
			Objects.requireNonNull(rectifiability);
			Objects.requireNonNull(traversalOutcome);
			Objects.requireNonNull(constructionFidelity);
			Objects.requireNonNull(evaluatorMethod);
			Objects.requireNonNull(metricMethod);
			Objects.requireNonNull(representationRole);
			Objects.requireNonNull(errorEvidence);
			Objects.requireNonNull(units);
			Objects.requireNonNull(provenance);
			contributions = List.copyOf(contributions);
			diagnostics = List.copyOf(diagnostics);
		}

		MetricValueKind valueKind() {
			return metricValue.kind();
		}

		OptionalDouble finiteValue() {
			return metricValue.finiteValue();
		}

		Optional<NumericGuarantee> numericGuarantee() {
			return errorEvidence.numericGuarantee();
		}

		boolean richDefined() {
			return true;
		}

		boolean scalarAdmissible() {
			boolean satisfiedTraversal = traversalOutcome.isEmpty()
					|| traversalOutcome.filter(outcome ->
							outcome == TraversalOutcome.TARGET_REACHED
									|| outcome == TraversalOutcome.WRAPPED_TO_START)
							.isPresent();
			boolean suitableGuarantee = numericGuarantee().stream().anyMatch(
					guarantee -> guarantee == NumericGuarantee.EXACT_ARITHMETIC
							|| guarantee == NumericGuarantee.CERTIFIED_ERROR_BOUND
							|| guarantee == NumericGuarantee.ESTIMATED_ERROR);
			return valueKind() == MetricValueKind.FINITE
					&& computationStatus == MetricComputationStatus.SUCCESS
					&& coverage == MetricCoverage.COMPLETE && satisfiedTraversal
					&& suitableGuarantee;
		}
	}

	static final class LocusMetricAggregator2D {
		LocusMetricResult2D aggregate(List<MetricContribution2D> input) {
			List<MetricContribution2D> contributions = input.stream()
					.sorted(Comparator.comparing(MetricContribution2D::branchKey)
							.thenComparing(MetricContribution2D::componentKey))
					.toList();
			if (contributions.isEmpty()) {
				return result(new FiniteMetricValue2D(0),
						MetricCoverage.COMPLETE, MetricComputationStatus.SUCCESS,
						MetricRectifiability.RECTIFIABLE,
						MetricErrorEvidence2D.exact(false,
								"empty-domain identity"),
						contributions, List.of("empty domain has complete zero length"));
			}
			boolean infinity = contributions.stream().anyMatch(contribution ->
					contribution.valueKind() == MetricValueKind.POSITIVE_INFINITY);
			boolean unresolved = contributions.stream().anyMatch(contribution ->
					contribution.computationStatus()
							!= MetricComputationStatus.SUCCESS);
			boolean known = contributions.stream().anyMatch(contribution ->
					contribution.valueKind() != MetricValueKind.ABSENT);
			MetricValue2D metricValue = infinity
					? new PositiveInfinityMetricValue2D()
					: known ? new FiniteMetricValue2D(
							compensatedFiniteSum(contributions))
							: new AbsentMetricValue2D();
			MetricCoverage coverage = unresolved ? MetricCoverage.INCOMPLETE
					: MetricCoverage.COMPLETE;
			MetricComputationStatus status = weakestStatus(contributions);
			MetricRectifiability rectifiability = rectifiability(contributions);
			MetricErrorEvidence2D errorEvidence = aggregateError(metricValue,
					coverage, contributions);
			List<String> diagnostics = unresolved
					? List.of("known contributions retained; unresolved contributions "
							+ "make coverage incomplete") : List.of();
			return result(metricValue, coverage, status, rectifiability,
					errorEvidence, contributions, diagnostics);
		}

		private static LocusMetricResult2D result(MetricValue2D metricValue,
				MetricCoverage coverage,
				MetricComputationStatus status,
				MetricRectifiability rectifiability,
				MetricErrorEvidence2D errorEvidence,
				List<MetricContribution2D> contributions,
				List<String> diagnostics) {
			return new LocusMetricResult2D(metricValue, coverage, status,
					rectifiability, Optional.empty(),
					ConstructionFidelity.SEMANTIC_NUMERICAL_EVALUATION,
					EvaluatorMethod.DIFFERENTIAL,
					MetricMethod.ADAPTIVE_DIFFERENTIAL_QUADRATURE,
					RepresentationRole.SEMANTIC_METRIC, errorEvidence,
					"construction-unit", "G7A test-private aggregate",
					contributions, diagnostics);
		}

		private static double compensatedFiniteSum(
				List<MetricContribution2D> contributions) {
			double sum = 0;
			double compensation = 0;
			for (MetricContribution2D contribution : contributions) {
				if (contribution.valueKind() != MetricValueKind.FINITE) {
					continue;
				}
				double adjusted = contribution.finiteValue().orElseThrow()
						- compensation;
				double next = sum + adjusted;
				compensation = next - sum - adjusted;
				sum = next;
			}
			return sum;
		}

		private static MetricComputationStatus weakestStatus(
				List<MetricContribution2D> contributions) {
			MetricComputationStatus weakest = MetricComputationStatus.SUCCESS;
			for (MetricContribution2D contribution : contributions) {
				if (statusRank(contribution.computationStatus())
						> statusRank(weakest)) {
					weakest = contribution.computationStatus();
				}
			}
			return weakest;
		}

		private static int statusRank(MetricComputationStatus status) {
			return switch (status) {
			case SUCCESS -> 0;
			case UNSUPPORTED -> 1;
			case LIMIT_NOT_ESTABLISHED -> 2;
			case NUMERICAL_FAILURE -> 3;
			case INVALID_QUERY -> 4;
			};
		}

		private static MetricRectifiability rectifiability(
				List<MetricContribution2D> contributions) {
			if (contributions.stream().anyMatch(contribution ->
					contribution.rectifiability()
							== MetricRectifiability.NON_RECTIFIABLE)) {
				return MetricRectifiability.NON_RECTIFIABLE;
			}
			if (contributions.stream().anyMatch(contribution ->
					contribution.rectifiability()
							== MetricRectifiability.UNDETERMINED)) {
				return MetricRectifiability.UNDETERMINED;
			}
			return MetricRectifiability.RECTIFIABLE;
		}

		private static NumericGuarantee weakestGuarantee(
				List<MetricContribution2D> contributions) {
			NumericGuarantee weakest = NumericGuarantee.EXACT_ARITHMETIC;
			for (MetricContribution2D contribution : contributions) {
				if (contribution.numericGuarantee().isPresent()
						&& guaranteeRank(contribution.numericGuarantee().get())
								> guaranteeRank(weakest)) {
					weakest = contribution.numericGuarantee().get();
				}
			}
			return weakest;
		}

		private static int guaranteeRank(NumericGuarantee guarantee) {
			return switch (guarantee) {
			case EXACT_ARITHMETIC -> 0;
			case CERTIFIED_ERROR_BOUND -> 1;
			case ESTIMATED_ERROR -> 2;
			case FLOATING_POINT_UNCERTIFIED -> 3;
			};
		}

		private static MetricErrorEvidence2D aggregateError(
				MetricValue2D metricValue, MetricCoverage coverage,
				List<MetricContribution2D> contributions) {
			if (metricValue.kind() != MetricValueKind.FINITE) {
				return MetricErrorEvidence2D.notApplicable(
						"non-finite aggregate value");
			}
			NumericGuarantee weakest = weakestGuarantee(contributions);
			if (weakest == NumericGuarantee.FLOATING_POINT_UNCERTIFIED
					|| contributions.stream().anyMatch(contribution ->
							contribution.errorEvidence().absoluteEvidence().state()
									== MetricErrorAmountState.NOT_ESTABLISHED)) {
				MetricErrorEvidence2D uncertified =
						MetricErrorEvidence2D.uncertified(
						"aggregate of uncertified contributions", List.of());
				return coverage == MetricCoverage.INCOMPLETE
						? uncertified.asReportedPartialValue() : uncertified;
			}
			double absolute = contributions.stream()
					.filter(contribution -> contribution.errorEvidence()
							.absoluteEvidence().amount().isPresent())
					.mapToDouble(contribution -> contribution.errorEvidence()
							.absoluteEvidence().amount().getAsDouble()).sum();
			double value = metricValue.finiteValue().orElseThrow();
			OptionalDouble relative = value > 0
					? OptionalDouble.of(absolute / value) : OptionalDouble.empty();
			MetricErrorEvidence2D result;
			if (weakest == NumericGuarantee.EXACT_ARITHMETIC) {
				result = MetricErrorEvidence2D.exact(value > 0,
						"exact contribution aggregation");
			} else if (weakest == NumericGuarantee.CERTIFIED_ERROR_BOUND) {
				result = MetricErrorEvidence2D.certified(absolute, relative,
						"sum of certified absolute bounds",
						"non-negative contribution bound sum");
			} else {
				result = MetricErrorEvidence2D.estimated(absolute, relative,
						"sum of estimated absolute errors",
						List.of("component estimates retain their assumptions"));
			}
			return coverage == MetricCoverage.INCOMPLETE
					? result.asReportedPartialValue() : result;
		}
	}
}

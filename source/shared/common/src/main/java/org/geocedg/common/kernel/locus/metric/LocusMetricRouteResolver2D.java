/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/**
 * Interprets between-position semantics and topology without integrating.
 */
public final class LocusMetricRouteResolver2D {

	/**
	 * Resolves one query against exactly one immutable semantic definition.
	 *
	 * @return route resolution without any integration
	 */
	public LocusMetricRoute2D resolve(BetweenPositionsMetricQuery query,
			LocusDefinition2D definition) {
		MetricPositionBinding2D start = query.getStart();
		MetricPositionBinding2D target = query.getTarget();
		LocusSemanticPosition2D startPosition = start.getSemanticPosition();
		LocusSemanticPosition2D targetPosition = target.getSemanticPosition();
		if (!query.getLocusIdentity().equals(definition.getLocusIdentity())) {
			return failure(query, definition, MetricRouteStatus.DIFFERENT_LOCUS,
					MetricDiagnosticCode2D.DIFFERENT_LOCUS,
					"Query locus identity does not match the semantic definition");
		}
		if (start.getEvaluationStatus()
					== MetricPositionEvaluationStatus.POSITION_STALE
				|| target.getEvaluationStatus()
					== MetricPositionEvaluationStatus.POSITION_STALE
				|| start.getSemanticRevision() != definition.getSemanticRevision()
				|| target.getSemanticRevision()
					!= definition.getSemanticRevision()) {
			return failure(query, definition, MetricRouteStatus.POSITION_STALE,
					MetricDiagnosticCode2D.POSITION_STALE,
					"Both bindings require explicit rebind to the current revision");
		}
		if (!start.isValid() || !target.isValid()) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.POSITION_OUTSIDE_DOMAIN,
					"Both positions must be valid revision-bound semantic addresses");
		}
		if (!startPosition.getLocusIdentity().equals(
				targetPosition.getLocusIdentity())) {
			return failure(query, definition, MetricRouteStatus.DIFFERENT_LOCUS,
					MetricDiagnosticCode2D.DIFFERENT_LOCUS,
					"Positions belong to different loci");
		}
		if (!startPosition.getBranchKey().equals(targetPosition.getBranchKey())) {
			return failure(query, definition, MetricRouteStatus.DIFFERENT_BRANCH,
					MetricDiagnosticCode2D.DIFFERENT_BRANCH,
					"Positions belong to different constructive branches");
		}
		LocusBranch2D branch =
				definition.getBranch(startPosition.getBranchKey());
		if (branch == null) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.BRANCH_MISSING,
					"Constructive branch disappeared before route resolution");
		}
		if (startPosition.equals(targetPosition)) {
			return resolveSamePosition(query, definition, branch);
		}
		String startComponent = requiredComponent(start);
		String targetComponent = requiredComponent(target);
		if (!startComponent.equals(targetComponent)) {
			return failure(query, definition,
					MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					MetricDiagnosticCode2D.DISCONTINUITY,
					"No boundary policy may cross an internal invalid-domain gap");
		}
		LocusInterval2D component = LocusMetricComponentKey2D.find(definition,
				branch, startComponent);
		if (component == null) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.POSITION_STALE,
					"Revision-scoped component key is absent");
		}
		double from = startPosition.getProviderCanonicalParameter();
		double to = targetPosition.getProviderCanonicalParameter();
		boolean movesIncreasing = movesIncreasing(query.getDirection(), branch);
		boolean directlyReachable = movesIncreasing ? to > from : to < from;
		if (directlyReachable) {
			return success(query, definition, branch,
					Collections.singletonList(segment(startComponent, from, to,
							query.getDirection(), MetricRouteSegmentRole.DIRECT)),
					true, false, true, TraversalOutcome.TARGET_REACHED,
					Collections.emptyList());
		}
		if (definition.getProvider().isPeriodic()) {
			if (branch.getValidDomainComponents().size() != 1) {
				return failure(query, definition,
						MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
						MetricDiagnosticCode2D.DISCONTINUITY,
						"Periodic traversal cannot cross an internal domain gap");
			}
			return acrossSeam(query, definition, branch, component,
					startComponent, from, to, MetricRouteSegmentRole.DIRECT);
		}
		return resolveOpenBoundary(query, definition, branch, component,
				startComponent, from, to, movesIncreasing);
	}

	private LocusMetricRoute2D resolveSamePosition(
			BetweenPositionsMetricQuery query, LocusDefinition2D definition,
			LocusBranch2D branch) {
		if (query.getSamePositionPolicy() == SamePositionPolicy.ZERO_LENGTH) {
			return success(query, definition, branch, Collections.emptyList(),
					true, false, true, TraversalOutcome.TARGET_REACHED,
					Collections.emptyList());
		}
		if (!definition.getProvider().isPeriodic()
				|| branch.getValidDomainComponents().size() != 1) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.TARGET_NOT_REACHABLE,
					"FULL_CYCLE requires one approved periodic component");
		}
		String key = requiredComponent(query.getStart());
		LocusInterval2D component = LocusMetricComponentKey2D.find(definition,
				branch, key);
		if (component == null) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.POSITION_STALE,
					"Revision-scoped component key is absent");
		}
		double parameter = query.getStart().getSemanticPosition()
				.getProviderCanonicalParameter();
		return acrossSeam(query, definition, branch, component, key, parameter,
				parameter, MetricRouteSegmentRole.PERIODIC_CYCLE);
	}

	private LocusMetricRoute2D acrossSeam(BetweenPositionsMetricQuery query,
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, String componentKey, double from,
			double to, MetricRouteSegmentRole role) {
		boolean increasing = movesIncreasing(query.getDirection(), branch);
		List<LocusMetricRouteSegment2D> segments = new ArrayList<>();
		if (increasing) {
			addIfNonZero(segments, segment(componentKey, from,
					component.getUpper(), query.getDirection(), role));
			addIfNonZero(segments, segment(componentKey, component.getLower(),
					to, query.getDirection(), role));
		} else {
			addIfNonZero(segments, segment(componentKey, from,
					component.getLower(), query.getDirection(), role));
			addIfNonZero(segments, segment(componentKey, component.getUpper(),
					to, query.getDirection(), role));
		}
		if (segments.isEmpty()) {
			segments.add(segment(componentKey, component.getLower(),
					component.getUpper(), query.getDirection(), role));
		}
		return success(query, definition, branch, segments, true, true, true,
				TraversalOutcome.TARGET_REACHED, Collections.emptyList());
	}

	private LocusMetricRoute2D resolveOpenBoundary(
			BetweenPositionsMetricQuery query, LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component,
			String componentKey, double from, double to,
			boolean movesIncreasing) {
		if (query.getBoundaryPolicy() == OpenBoundaryPolicy.STRICT) {
			return failure(query, definition,
					MetricRouteStatus.TARGET_NOT_REACHABLE,
					MetricDiagnosticCode2D.TARGET_NOT_REACHABLE,
					"STRICT rejects a target unreachable in the chosen direction");
		}
		int componentIndex = LocusMetricComponentKey2D.indexOf(definition,
				branch, componentKey);
		boolean atGlobalBoundary = movesIncreasing
				? componentIndex == branch.getValidDomainComponents().size() - 1
				: componentIndex == 0;
		if (!atGlobalBoundary) {
			return failure(query, definition,
					MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					MetricDiagnosticCode2D.DISCONTINUITY,
					"Traversal reaches an internal invalid-domain gap");
		}
		double boundary = movesIncreasing ? component.getUpper()
				: component.getLower();
		LocusMetricRouteSegment2D toBoundary = segment(componentKey, from,
				boundary, query.getDirection(),
				MetricRouteSegmentRole.TO_GLOBAL_BOUNDARY);
		if (query.getBoundaryPolicy() == OpenBoundaryPolicy.STOP_AT_END) {
			return success(query, definition, branch,
					Collections.singletonList(toBoundary), false, false, true,
					TraversalOutcome.STOPPED_AT_BOUNDARY,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.STOPPED_AT_BOUNDARY,
							"Reported value ends at the global boundary; target "
									+ "was not reached")));
		}
		if (branch.getValidDomainComponents().size() != 1) {
			return failure(query, definition,
					MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					MetricDiagnosticCode2D.DISCONTINUITY,
					"WRAP cannot cross an internal invalid-domain gap");
		}
		double restart = movesIncreasing ? component.getLower()
				: component.getUpper();
		LocusMetricRouteSegment2D fromBoundary = segment(componentKey, restart,
				to, query.getDirection(),
				MetricRouteSegmentRole.FROM_GLOBAL_BOUNDARY);
		List<LocusMetricRouteSegment2D> segments = new ArrayList<>();
		addIfNonZero(segments, toBoundary);
		addIfNonZero(segments, fromBoundary);
		return success(query, definition, branch, segments, true, true, false,
				TraversalOutcome.WRAPPED_TO_START,
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.WRAP_CONVENTION,
						"WRAP is a metric convention and creates no geometric "
								+ "connection or incidence")));
	}

	private static boolean movesIncreasing(TraversalDirection direction,
			LocusBranch2D branch) {
		boolean forwardIncreasing =
				branch.getOrientation() == Orientation.INCREASING;
		return direction == TraversalDirection.FORWARD
				? forwardIncreasing : !forwardIncreasing;
	}

	private static String requiredComponent(MetricPositionBinding2D binding) {
		Optional<String> component = binding.getResolvedValidComponentKey();
		if (component.isEmpty()) {
			throw new IllegalStateException("A valid binding needs a component");
		}
		return component.get();
	}

	private static LocusMetricRouteSegment2D segment(String componentKey,
			double from, double to, TraversalDirection direction,
			MetricRouteSegmentRole role) {
		return new LocusMetricRouteSegment2D(componentKey, from, to, direction,
				role);
	}

	private static void addIfNonZero(
			List<LocusMetricRouteSegment2D> segments,
			LocusMetricRouteSegment2D segment) {
		if (segment.getStartCanonicalParameter()
				!= segment.getEndCanonicalParameter()) {
			segments.add(segment);
		}
	}

	private static LocusMetricRoute2D success(
			BetweenPositionsMetricQuery query, LocusDefinition2D definition,
			LocusBranch2D branch,
			List<LocusMetricRouteSegment2D> segments, boolean reached,
			boolean wrapped, boolean connected, TraversalOutcome outcome,
			List<MetricDiagnostic2D> diagnostics) {
		return new LocusMetricRoute2D(definition.getLocusIdentity(),
				definition.getSemanticRevision(), branch.getBranchKey(), segments,
				query.getDirection(), query.getBoundaryPolicy(), reached, wrapped,
				connected, MetricRouteStatus.RESOLVED, outcome, diagnostics);
	}

	private static LocusMetricRoute2D failure(
			BetweenPositionsMetricQuery query, LocusDefinition2D definition,
			MetricRouteStatus status, MetricDiagnosticCode2D code,
			String message) {
		return new LocusMetricRoute2D(definition.getLocusIdentity(),
				definition.getSemanticRevision(),
				query.getStart().getSemanticPosition().getBranchKey(),
				Collections.emptyList(), query.getDirection(),
				query.getBoundaryPolicy(), false, false, false, status,
				status == MetricRouteStatus.DISCONTINUITY_ENCOUNTERED
						? TraversalOutcome.DISCONTINUITY_ENCOUNTERED
						: TraversalOutcome.TARGET_NOT_REACHABLE,
				Collections.singletonList(new MetricDiagnostic2D(code, message)));
	}
}

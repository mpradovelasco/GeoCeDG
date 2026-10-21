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
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
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
		ContinuousComponent component = LocusMetricComponentKey2D.findComponent(definition,
				branch, startComponent);
		if (component == null) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.POSITION_STALE,
					"Revision-scoped component key is absent");
		}
		double from = startPosition.getProviderCanonicalParameter();
		double to = targetPosition.getProviderCanonicalParameter();
		// PRE-G9B-CU2 section 26.4: the route follows the arc coordinate of its component.
		double epsilon = definition.getProvider().getDomainEpsilon();
		double fromCoordinate = component.getArcCoordinate(from, epsilon);
		double toCoordinate = component.getArcCoordinate(to, epsilon);
		if (Double.isNaN(fromCoordinate) || Double.isNaN(toCoordinate)) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.POSITION_OUTSIDE_DOMAIN,
					"A position lies outside its certified component");
		}
		boolean movesIncreasing = movesIncreasing(query.getDirection(), branch);
		boolean directlyReachable = movesIncreasing ? toCoordinate > fromCoordinate
				: toCoordinate < fromCoordinate;
		if (directlyReachable) {
			return success(query, definition, branch,
					Collections.singletonList(segment(startComponent,
							lifted(branch, component, from), lifted(branch, component, to),
							query.getDirection(), MetricRouteSegmentRole.DIRECT)),
					true, false, true, TraversalOutcome.TARGET_REACHED,
					Collections.emptyList());
		}
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		if (structure.isClosedPeriodic()) {
			return acrossSeam(query, definition, branch, component, startComponent, from,
					to, MetricRouteSegmentRole.DIRECT);
		}
		if (structure.getTopology() == LocusExistenceStructure2D.DomainTopology.PERIODIC) {
			return resolveOpenArc(query, definition, branch, component, startComponent,
					from, movesIncreasing);
		}
		return resolveOpenBoundary(query, definition, branch, component.getInterval(),
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
		// Single closure authority (locus-v2-existence-components.md section 15.3).
		if (!branch.getExistenceStructure().isClosedPeriodic()) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.TARGET_NOT_REACHABLE,
					"FULL_CYCLE requires one approved periodic component");
		}
		String key = requiredComponent(query.getStart());
		ContinuousComponent component = LocusMetricComponentKey2D.findComponent(definition,
				branch, key);
		if (component == null) {
			return failure(query, definition, MetricRouteStatus.INVALID_QUERY,
					MetricDiagnosticCode2D.POSITION_STALE,
					"Revision-scoped component key is absent");
		}
		double parameter = query.getStart().getSemanticPosition()
				.getProviderCanonicalParameter();
		return acrossSeam(query, definition, branch, component, key, parameter, parameter,
				MetricRouteSegmentRole.PERIODIC_CYCLE);
	}

	/**
	 * Parameter of a route segment on the component: on an arc that crosses the seam the
	 * route follows the arc in the lifted range of the component, from its start, and never
	 * wraps; every other parameter is already in that range and is kept bit for bit.
	 */
	private static double lifted(LocusBranch2D branch, ContinuousComponent component,
			double parameter) {
		if (!component.isArc() || parameter >= component.getStart()) {
			return parameter;
		}
		LocusInterval2D domain = branch.getExistenceStructure().getCanonicalDomain();
		return parameter + (domain.getUpper() - domain.getLower());
	}

	/**
	 * A target behind the start on an open arc of a periodic domain: the arc is an open
	 * curve whose ends are its own; {@code wrap-to-start} and the seam wrap need a certified
	 * closed periodic component (consumption design section 3, V-CON-2).
	 */
	private LocusMetricRoute2D resolveOpenArc(BetweenPositionsMetricQuery query,
			LocusDefinition2D definition, LocusBranch2D branch,
			ContinuousComponent component, String componentKey, double from,
			boolean movesIncreasing) {
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		if (structure.getContinuousValidComponents().size() != 1) {
			return failure(query, definition,
					MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					MetricDiagnosticCode2D.DISCONTINUITY,
					"Periodic traversal cannot cross an internal domain gap");
		}
		if (query.getBoundaryPolicy() == OpenBoundaryPolicy.STRICT) {
			return failure(query, definition,
					MetricRouteStatus.TARGET_NOT_REACHABLE,
					MetricDiagnosticCode2D.TARGET_NOT_REACHABLE,
					"STRICT rejects a target unreachable in the chosen direction");
		}
		if (query.getBoundaryPolicy() == OpenBoundaryPolicy.WRAP_TO_START) {
			return failure(query, definition, MetricRouteStatus.TARGET_NOT_REACHABLE,
					MetricDiagnosticCode2D.TOPOLOGY_NOT_ESTABLISHED,
					"WRAP_TO_START requires a certified closed periodic component");
		}
		if (structure.getCompleteness() != LocusExistenceStructure2D.Completeness.COMPLETE) {
			return failure(query, definition,
					MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					MetricDiagnosticCode2D.TOPOLOGY_NOT_ESTABLISHED,
					"The end of the arc is not an established global boundary");
		}
		// The end of the arc in its lifted range; an excluded end is measured through its
		// one-sided limit (LocusDefinition2D.evaluateInComponent).
		double boundary = movesIncreasing ? component.getInterval().getUpper()
				: component.getInterval().getLower();
		return success(query, definition, branch,
				Collections.singletonList(segment(componentKey,
						lifted(branch, component, from), boundary, query.getDirection(),
						MetricRouteSegmentRole.TO_GLOBAL_BOUNDARY)),
				false, false, true, TraversalOutcome.STOPPED_AT_BOUNDARY,
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.STOPPED_AT_BOUNDARY,
						"Reported value ends at the global boundary; target "
								+ "was not reached")));
	}

	/**
	 * Wraps around the certified closed periodic component through its closing point, in
	 * the lifted range of the component; for a component that starts at the seam the
	 * parameters are the canonical ones.
	 */
	private LocusMetricRoute2D acrossSeam(BetweenPositionsMetricQuery query,
			LocusDefinition2D definition, LocusBranch2D branch,
			ContinuousComponent certified, String componentKey, double canonicalFrom,
			double canonicalTo, MetricRouteSegmentRole role) {
		LocusInterval2D component = certified.getInterval();
		double from = lifted(branch, certified, canonicalFrom);
		double to = lifted(branch, certified, canonicalTo);
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
		// The global boundary is a fact of the complete inventory only; the index and the
		// count come from the same list (PRE-G9B-CU2, V-CON-8).
		List<ContinuousComponent> components =
				branch.getExistenceStructure().getContinuousValidComponents();
		if (branch.getExistenceStructure().getCompleteness()
				!= LocusExistenceStructure2D.Completeness.COMPLETE) {
			return failure(query, definition,
					MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					MetricDiagnosticCode2D.TOPOLOGY_NOT_ESTABLISHED,
					"Global boundaries are not established without a complete inventory");
		}
		int componentIndex = LocusMetricComponentKey2D.indexOf(definition,
				branch, componentKey);
		boolean atGlobalBoundary = movesIncreasing
				? componentIndex == components.size() - 1
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
		if (components.size() != 1) {
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

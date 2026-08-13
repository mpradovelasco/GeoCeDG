/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRouteResolver2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionEvaluationStatus;
import org.geocedg.common.kernel.locus.metric.MetricRouteStatus;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.junit.jupiter.api.Test;

/** Productive semantic-position and route-resolver contract tests. */
class LocusMetricProductiveRouteTest {
	private final LocusMetricRouteResolver2D resolver =
			new LocusMetricRouteResolver2D();

	@Test
	void forwardAndReverseSelectDifferentNonNegativeRoutes() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusMetricRoute2D forward = resolve(definition, -2, 3,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		LocusMetricRoute2D reverse = resolve(definition, 3, -2,
				TraversalDirection.REVERSE, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.RESOLVED,
				forward.getRouteStatus());
		assertEquals(MetricRouteStatus.RESOLVED,
				reverse.getRouteStatus());
		assertEquals(-2, forward.getOrderedRouteSegments().get(0)
				.getStartCanonicalParameter());
		assertEquals(3, reverse.getOrderedRouteSegments().get(0)
				.getStartCanonicalParameter());
		assertEquals(TraversalOutcome.TARGET_REACHED,
				forward.getTraversalOutcome());
		assertEquals(TraversalOutcome.TARGET_REACHED,
				reverse.getTraversalOutcome());
	}

	@Test
	void equalSemanticPositionZeroLengthHasNoRouteSegment() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusMetricRoute2D route = resolve(definition, 1, 1,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		assertTrue(route.getOrderedRouteSegments().isEmpty());
		assertTrue(route.isTargetReached());
		assertFalse(route.isWrapped());
	}

	@Test
	void fullCycleRequiresApprovedPeriodicSemantics() {
		LocusMetricRoute2D open = resolve(line(false,
				G7BMetricFixtures.components(-4, 4)), 1, 1,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.FULL_CYCLE);
		LocusMetricRoute2D periodic = resolve(line(true,
				G7BMetricFixtures.components(-4, 4)), 1, 1,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.FULL_CYCLE);
		assertEquals(MetricRouteStatus.INVALID_QUERY, open.getRouteStatus());
		assertEquals(MetricRouteStatus.RESOLVED,
				periodic.getRouteStatus());
		assertEquals(2, periodic.getOrderedRouteSegments().size());
		assertTrue(periodic.isWrapped());
		assertTrue(periodic.isGeometricallyConnected());
	}

	@Test
	void periodicSeamIsResolvedWithoutCartesianEqualityInference() {
		LocusDefinition2D definition = line(true,
				G7BMetricFixtures.components(-4, 4));
		LocusMetricRoute2D route = resolve(definition, 3, -3,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(2, route.getOrderedRouteSegments().size());
		assertTrue(route.isWrapped());
		assertTrue(route.isGeometricallyConnected());
	}

	@Test
	void stopPublishesPartialBoundaryRouteNotCompleteAB() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusMetricRoute2D route = resolve(definition, 2, -2,
				TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STOP_AT_END,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(TraversalOutcome.STOPPED_AT_BOUNDARY,
				route.getTraversalOutcome());
		assertFalse(route.isTargetReached());
		assertFalse(route.isWrapped());
		assertEquals(4, route.getOrderedRouteSegments().get(0)
				.getEndCanonicalParameter());
	}

	@Test
	void wrapIsTwoSegmentsAndExplicitlyGeometricallyDisconnected() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusMetricRoute2D route = resolve(definition, 2, -2,
				TraversalDirection.FORWARD,
				OpenBoundaryPolicy.WRAP_TO_START,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(TraversalOutcome.WRAPPED_TO_START,
				route.getTraversalOutcome());
		assertEquals(2, route.getOrderedRouteSegments().size());
		assertTrue(route.isTargetReached());
		assertTrue(route.isWrapped());
		assertFalse(route.isGeometricallyConnected());
		assertEquals(4, route.getOrderedRouteSegments().get(0)
				.getEndCanonicalParameter());
		assertEquals(-4, route.getOrderedRouteSegments().get(1)
				.getStartCanonicalParameter());
	}

	@Test
	void strictRejectsBeforeAnyMetricIntegration() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusMetricRoute2D route = resolve(definition, 2, -2,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.TARGET_NOT_REACHABLE,
				route.getRouteStatus());
		assertEquals(TraversalOutcome.TARGET_NOT_REACHABLE,
				route.getTraversalOutcome());
		assertTrue(route.getOrderedRouteSegments().isEmpty());
	}

	@Test
	void noPolicyCrossesAnInternalInvalidDomainGap() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, -1, 1, 4));
		for (OpenBoundaryPolicy policy : OpenBoundaryPolicy.values()) {
			LocusMetricRoute2D route = resolve(definition, -2, 2,
					TraversalDirection.FORWARD, policy,
					SamePositionPolicy.ZERO_LENGTH);
			assertEquals(MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
					route.getRouteStatus(), policy.toString());
			assertEquals(TraversalOutcome.DISCONTINUITY_ENCOUNTERED,
					route.getTraversalOutcome(), policy.toString());
		}
	}

	@Test
	void branchMismatchIsNotRepairedAtASelfIntersection() {
		LocusDefinition2D definition = G7BMetricFixtures.definitionWithBranches(
				"self-x", 1, false,
				List.of(
						G7BMetricFixtures.branch("branch-a", -4, 4,
								G7BMetricFixtures.components(-4, 4)),
						G7BMetricFixtures.branch("branch-b", -4, 4,
								G7BMetricFixtures.components(-4, 4))),
				parameter -> new LocusPoint2D(parameter * parameter - 1,
						parameter * (parameter * parameter - 1)));
		MetricPositionBinding2D first =
				G7BMetricFixtures.bind(definition, "branch-a", 1);
		MetricPositionBinding2D second =
				G7BMetricFixtures.bind(definition, "branch-b", 1);
		assertEquals(first.getEvaluatedPoint(), second.getEvaluatedPoint());
		BetweenPositionsMetricQuery query = new BetweenPositionsMetricQuery(first,
				second, TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH,
				org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D
						.initial());
		assertEquals(MetricRouteStatus.DIFFERENT_BRANCH,
				resolver.resolve(query, definition).getRouteStatus());
	}

	@Test
	void componentSplitDoesNotChangeDurablePositionButMakesOldBindingStale() {
		LocusDefinition2D before = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusDefinition2D after = G7BMetricFixtures.definition("route", 2,
				false, G7BMetricFixtures.components(-4, 0, 0.5, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		MetricPositionBinding2D old =
				G7BMetricFixtures.bind(before, G7BMetricFixtures.BRANCH, -2);
		MetricPositionBinding2D rebound =
				new LocusMetricPositionBinder2D().bind(
						old.getSemanticPosition(), after);
		assertEquals(old.getSemanticPosition(), rebound.getSemanticPosition());
		assertNotEquals(old.getResolvedValidComponentKey(),
				rebound.getResolvedValidComponentKey());
		assertEquals(MetricPositionEvaluationStatus.POSITION_STALE,
				new LocusMetricPositionBinder2D().requireRevision(old, 2)
						.getEvaluationStatus());
		assertEquals(MetricPositionEvaluationStatus.VALID,
				rebound.getEvaluationStatus());
	}

	@Test
	void providerVersionMismatchFailsWithoutCoordinateRepair() {
		LocusDefinition2D definition = line(false,
				G7BMetricFixtures.components(-4, 4));
		LocusSemanticPosition2D incompatible = new LocusSemanticPosition2D(
				definition.getLocusIdentity(), G7BMetricFixtures.BRANCH,
				"foreign-provider/v9", 0);
		MetricPositionBinding2D binding =
				new LocusMetricPositionBinder2D().bind(incompatible, definition);
		assertEquals(
				MetricPositionEvaluationStatus.PROVIDER_VERSION_MISMATCH,
				binding.getEvaluationStatus());
		assertTrue(binding.getEvaluatedPoint().isEmpty());
		assertTrue(binding.getResolvedValidComponentKey().isEmpty());
	}

	private LocusMetricRoute2D resolve(LocusDefinition2D definition,
			double start, double target, TraversalDirection direction,
			OpenBoundaryPolicy boundary, SamePositionPolicy same) {
		return resolver.resolve(G7BMetricFixtures.between(definition, start,
				target, direction, boundary, same), definition);
	}

	private static LocusDefinition2D line(boolean periodic,
			List<LocusInterval2D> components) {
		return G7BMetricFixtures.definition("route", 1, periodic, components,
				parameter -> new LocusPoint2D(parameter, 0));
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G7AMetricSemanticModel.AbsentMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.BindingStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.BoundaryPolicy;
import org.geocedg.common.locus.G7AMetricSemanticModel.BranchRevision;
import org.geocedg.common.locus.G7AMetricSemanticModel.ConstructionFidelity;
import org.geocedg.common.locus.G7AMetricSemanticModel.Direction;
import org.geocedg.common.locus.G7AMetricSemanticModel.EvaluatorMethod;
import org.geocedg.common.locus.G7AMetricSemanticModel.FiniteMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.LocusMetricAggregator2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.LocusMetricResult2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.LocusMetricRoute2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.LocusMetricRouteResolver2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.LocusSemanticPosition2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricComputationStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricContribution2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricCoverage;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorAmountState;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorEvidence2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorEvidenceScope;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricMethod;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricPositionBinding2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricRectifiability;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValueKind;
import org.geocedg.common.locus.G7AMetricSemanticModel.PositiveInfinityMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.RepresentationRole;
import org.geocedg.common.locus.G7AMetricSemanticModel.RouteStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.SamePositionPolicy;
import org.geocedg.common.locus.G7AMetricSemanticModel.TotalLocusMetricQuery;
import org.geocedg.common.locus.G7AMetricSemanticModel.TraversalOutcome;
import org.geocedg.common.locus.G7AMetricSemanticModel.ValidComponent;
import org.junit.jupiter.api.Test;

/** Executable G7A semantics, route, aggregation and scalar-facet evidence. */
class G7AMetricSemanticCharacterizationTest {
	private static final String LOCUS = "g7a.fixture.locus";
	private static final String BRANCH = "sheet.main";
	private static final String PROVIDER = "provider/v1";
	private final LocusMetricRouteResolver2D resolver =
			new LocusMetricRouteResolver2D();
	private final LocusMetricAggregator2D aggregator =
			new LocusMetricAggregator2D();

	@Test
	void semanticPositionIsDurableWhileBindingIsRevisionScoped() {
		LocusSemanticPosition2D position = position(0.25);
		BranchRevision original = branch(1, false,
				component("r1.whole", 0, 1));
		MetricPositionBinding2D first = G7AMetricSemanticModel.bind(position,
				original);
		BranchRevision split = branch(2, false,
				component("r2.left", 0, 0.4),
				component("r2.right", 0.6, 1));

		MetricPositionBinding2D stale = G7AMetricSemanticModel.useAtRevision(
				first, split.semanticRevision());
		MetricPositionBinding2D rebound = G7AMetricSemanticModel.bind(position,
				split);

		assertEquals(BindingStatus.VALID, first.evaluationStatus());
		assertEquals(BindingStatus.POSITION_STALE, stale.evaluationStatus());
		assertEquals(position, stale.semanticPosition());
		assertEquals(position, rebound.semanticPosition());
		assertEquals("r2.left", rebound.resolvedValidComponentKey());
		assertNotEquals(first.resolvedValidComponentKey(),
				rebound.resolvedValidComponentKey());
		assertTrue(stale.diagnostics().get(0).contains("coordinate repair"));
	}

	@Test
	void semanticPositionPreservesPreimageAndProviderIdentity() {
		LocusSemanticPosition2D firstPreimage = position(-1);
		LocusSemanticPosition2D secondPreimage = position(1);
		assertNotEquals(firstPreimage, secondPreimage);
		G7AMetricSemanticModel.Point firstImage = crossing(-1);
		G7AMetricSemanticModel.Point secondImage = crossing(1);
		assertEquals(firstImage, secondImage);

		LocusSemanticPosition2D providerUpgrade =
				new LocusSemanticPosition2D(LOCUS, BRANCH, "provider/v2", -1);
		assertNotEquals(firstPreimage, providerUpgrade);

		BranchRevision missing = new BranchRevision(LOCUS, 2, "other", false,
				List.of(component("other", -2, 2)));
		assertEquals(BindingStatus.BRANCH_MISSING,
				G7AMetricSemanticModel.bind(firstPreimage, missing)
						.evaluationStatus());
		assertEquals(BindingStatus.VALID,
				G7AMetricSemanticModel.bind(firstPreimage,
						branch(3, false, component("reappeared", -2, 2)))
						.evaluationStatus());
	}

	@Test
	void resolverSeparatesDirectionSamePositionAndPeriodicCycle() {
		BranchRevision open = branch(1, false, component("open", 0, 10));
		LocusMetricRoute2D forward = resolve(open, 2, 8, Direction.FORWARD,
				BoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH);
		LocusMetricRoute2D reverse = resolve(open, 8, 2, Direction.REVERSE,
				BoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH);
		assertEquals(6, routeLength(forward), 0);
		assertEquals(6, routeLength(reverse), 0);
		assertEquals(TraversalOutcome.TARGET_REACHED,
				forward.traversalOutcome());
		assertEquals(TraversalOutcome.TARGET_REACHED,
				reverse.traversalOutcome());

		LocusMetricRoute2D zero = resolve(open, 4, 4, Direction.FORWARD,
				BoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH);
		assertEquals(0, zero.orderedRouteSegments().size());
		assertEquals(0, routeLength(zero), 0);

		LocusMetricRoute2D rejectedFull = resolve(open, 4, 4,
				Direction.FORWARD, BoundaryPolicy.STRICT,
				SamePositionPolicy.FULL_CYCLE);
		assertEquals(RouteStatus.INVALID_QUERY, rejectedFull.routeStatus());

		BranchRevision periodic = branch(1, true,
				component("cycle", 0, 10));
		LocusMetricRoute2D full = resolve(periodic, 4, 4, Direction.FORWARD,
				BoundaryPolicy.STRICT, SamePositionPolicy.FULL_CYCLE);
		assertEquals(10, routeLength(full), 0);
		assertTrue(full.geometricallyConnected());
		assertTrue(full.wrapped());
		assertEquals(2, full.orderedRouteSegments().size());

		LocusMetricRoute2D seam = resolve(periodic, 8, 2, Direction.FORWARD,
				BoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH);
		assertEquals(4, routeLength(seam), 0);
		assertTrue(seam.geometricallyConnected());
		assertEquals(2, seam.orderedRouteSegments().size());
		for (double seamPosition : List.of(1.0, 4.0, 8.0)) {
			assertEquals(10, routeLength(resolve(periodic, seamPosition,
					seamPosition, Direction.REVERSE, BoundaryPolicy.STRICT,
					SamePositionPolicy.FULL_CYCLE)), 0);
		}
	}

	@Test
	void includedGlobalEndpointIsReachableButExcludedEndpointCannotBind() {
		BranchRevision included = branch(1, false, component("whole", 0, 10));
		assertEquals(8, routeLength(resolve(included, 2, 10,
				Direction.FORWARD, BoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH)), 0);

		BranchRevision excluded = branch(1, false,
				new ValidComponent("open-start", 0, 10, false, true));
		assertEquals(BindingStatus.POSITION_OUTSIDE_DOMAIN,
				bind(excluded, position(0)).evaluationStatus());
	}

	@Test
	void openPoliciesProduceDistinctSemanticOutcomes() {
		BranchRevision open = branch(1, false, component("open", 0, 10));
		LocusMetricRoute2D stop = resolve(open, 8, 2, Direction.FORWARD,
				BoundaryPolicy.STOP_AT_END, SamePositionPolicy.ZERO_LENGTH);
		assertEquals(2, routeLength(stop), 0);
		assertFalse(stop.targetReached());
		assertFalse(stop.wrapped());
		assertEquals(TraversalOutcome.STOPPED_AT_BOUNDARY,
				stop.traversalOutcome());

		LocusMetricRoute2D wrap = resolve(open, 8, 2, Direction.FORWARD,
				BoundaryPolicy.WRAP_TO_START, SamePositionPolicy.ZERO_LENGTH);
		assertEquals(4, routeLength(wrap), 0);
		assertEquals(2, wrap.orderedRouteSegments().size());
		assertTrue(wrap.targetReached());
		assertTrue(wrap.wrapped());
		assertFalse(wrap.geometricallyConnected());
		assertEquals(TraversalOutcome.WRAPPED_TO_START,
				wrap.traversalOutcome());

		LocusMetricRoute2D strict = resolve(open, 8, 2, Direction.FORWARD,
				BoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH);
		assertEquals(RouteStatus.TARGET_NOT_REACHABLE, strict.routeStatus());
		assertEquals(0, strict.orderedRouteSegments().size());
		assertFalse(strict.targetReached());

		LocusMetricRoute2D reverseWrap = resolve(open, 2, 8,
				Direction.REVERSE, BoundaryPolicy.WRAP_TO_START,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(4, routeLength(reverseWrap), 0);
		assertFalse(reverseWrap.geometricallyConnected());
	}

	@Test
	void noBoundaryPolicyCrossesInternalGapOrBranchBoundary() {
		BranchRevision split = branch(1, false,
				component("left", 0, 4), component("right", 6, 10));
		for (BoundaryPolicy policy : BoundaryPolicy.values()) {
			LocusMetricRoute2D route = resolve(split, 2, 8,
					Direction.FORWARD, policy, SamePositionPolicy.ZERO_LENGTH);
			assertEquals(RouteStatus.DISCONTINUITY_ENCOUNTERED,
					route.routeStatus());
			assertEquals(TraversalOutcome.DISCONTINUITY_ENCOUNTERED,
					route.traversalOutcome());
			assertTrue(route.orderedRouteSegments().isEmpty());
		}

		BranchRevision branch = branch(1, false, component("whole", 0, 10));
		MetricPositionBinding2D start = bind(branch, position(2));
		LocusSemanticPosition2D otherPosition = new LocusSemanticPosition2D(
				LOCUS, "other.branch", PROVIDER, 8);
		BranchRevision otherBranch = new BranchRevision(LOCUS, 1,
				"other.branch", false, List.of(component("other", 0, 10)));
		MetricPositionBinding2D target = G7AMetricSemanticModel.bind(
				otherPosition, otherBranch);
		LocusMetricRoute2D mismatch = resolver.resolve(
				new G7AMetricSemanticModel.BetweenPositionsMetricQuery(start, target,
						Direction.FORWARD, BoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH), branch);
		assertEquals(RouteStatus.DIFFERENT_BRANCH, mismatch.routeStatus());
	}

	@Test
	void completeAggregationCountsEveryConstructiveComponentExactlyOnce() {
		TotalLocusMetricQuery query = new TotalLocusMetricQuery(LOCUS, 7);
		assertEquals(LOCUS, query.locusIdentity());
		List<MetricContribution2D> contributions = List.of(
				finite("branch-a", "component-1", 2),
				finite("branch-a", "component-2", 3),
				finite("branch-b", "coincident-image", 2));
		LocusMetricResult2D total = aggregator.aggregate(contributions);
		assertEquals(MetricValueKind.FINITE, total.valueKind());
		assertEquals(7, total.finiteValue().orElseThrow(), 0);
		assertEquals(3, total.contributions().size());
		assertEquals(MetricCoverage.COMPLETE, total.coverage());
		assertTrue(total.scalarAdmissible());

		LocusMetricResult2D noDomain = aggregator.aggregate(List.of());
		assertEquals(0, noDomain.finiteValue().orElseThrow(), 0);
		assertEquals(MetricCoverage.COMPLETE, noDomain.coverage());
		assertEquals(MetricRectifiability.RECTIFIABLE,
				noDomain.rectifiability());

		LocusMetricResult2D isolated = aggregator.aggregate(
				List.of(finite("branch", "isolated", 0)));
		LocusMetricResult2D collapsed = aggregator.aggregate(
				List.of(finite("branch", "collapsed", 0)));
		assertEquals(0, isolated.finiteValue().orElseThrow(), 0);
		assertEquals(0, collapsed.finiteValue().orElseThrow(), 0);
		assertTrue(isolated.scalarAdmissible());
		assertTrue(collapsed.scalarAdmissible());
		assertEquals(MetricErrorAmountState.NOT_APPLICABLE,
				noDomain.errorEvidence().relativeEvidence().state());
	}

	@Test
	void richAggregateKeepsInfiniteAndUnsupportedAxesOrthogonal() {
		MetricContribution2D infinity = new MetricContribution2D("a", "line",
				new PositiveInfinityMetricValue2D(),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.NON_RECTIFIABLE,
				MetricErrorEvidence2D.notApplicable("analytic whole line"),
				"analytic whole line",
				List.of("established divergent total variation"));
		MetricContribution2D unsupported = new MetricContribution2D("b",
				"unknown", new AbsentMetricValue2D(),
				MetricComputationStatus.UNSUPPORTED,
				MetricRectifiability.UNDETERMINED,
				MetricErrorEvidence2D.notApplicable(
						"unsupported metric capability"),
				"evaluator without sufficient assumptions",
				List.of("no defensible metric capability"));

		LocusMetricResult2D result = aggregator.aggregate(
				List.of(unsupported, infinity));
		assertEquals(MetricValueKind.POSITIVE_INFINITY, result.valueKind());
		assertTrue(result.finiteValue().isEmpty());
		assertEquals(MetricCoverage.INCOMPLETE, result.coverage());
		assertEquals(MetricComputationStatus.UNSUPPORTED,
				result.computationStatus());
		assertEquals(MetricRectifiability.NON_RECTIFIABLE,
				result.rectifiability());
		assertEquals(2, result.contributions().size());
		assertFalse(result.scalarAdmissible());
		assertTrue(result.richDefined());

		LocusMetricResult2D finiteAndUnsupported = aggregator.aggregate(
				List.of(finite("a", "known", 2), unsupported));
		assertEquals(MetricValueKind.FINITE,
				finiteAndUnsupported.valueKind());
		assertEquals(2, finiteAndUnsupported.finiteValue().orElseThrow(), 0);
		assertEquals(MetricCoverage.INCOMPLETE,
				finiteAndUnsupported.coverage());
		assertEquals(MetricComputationStatus.UNSUPPORTED,
				finiteAndUnsupported.computationStatus());
		assertEquals(MetricErrorEvidenceScope.REPORTED_PARTIAL_VALUE,
				finiteAndUnsupported.errorEvidence().scope());
		assertFalse(finiteAndUnsupported.scalarAdmissible());

		MetricContribution2D failure = new MetricContribution2D("c", "failed",
				new AbsentMetricValue2D(),
				MetricComputationStatus.NUMERICAL_FAILURE,
				MetricRectifiability.UNDETERMINED,
				MetricErrorEvidence2D.notApplicable("injected failure"),
				"injected failure",
				List.of("exception converted to typed failure"));
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				aggregator.aggregate(List.of(unsupported, failure))
						.computationStatus());
	}

	@Test
	void aggregationIsOrderIndependentAndPropagatesWeakestGuarantee() {
		List<MetricContribution2D> contributions = List.of(
				finite("b", "2", 1e16),
				finite("a", "1", 1),
				estimated("c", "3", 3, 1e-8));
		List<MetricContribution2D> reverse = new ArrayList<>(contributions);
		Collections.reverse(reverse);
		LocusMetricResult2D first = aggregator.aggregate(contributions);
		LocusMetricResult2D second = aggregator.aggregate(reverse);
		assertEquals(first.finiteValue().orElseThrow(),
				second.finiteValue().orElseThrow(), 0);
		assertEquals(first.errorEvidence(), second.errorEvidence());
		assertEquals(1e-8, first.errorEvidence().absoluteEvidence().amount()
				.orElseThrow(), 0);
		assertEquals(NumericGuarantee.ESTIMATED_ERROR,
				first.numericGuarantee().orElseThrow());
		assertEquals(List.of("a", "b", "c"), first.contributions().stream()
				.map(MetricContribution2D::branchKey).toList());
	}

	@Test
	void scalarAdmissibilityDoesNotEqualRichDefinedState() {
		assertTrue(rich(MetricValueKind.FINITE, MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, null).scalarAdmissible());
		assertTrue(rich(MetricValueKind.FINITE, MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS,
				TraversalOutcome.WRAPPED_TO_START).scalarAdmissible());
		assertFalse(rich(MetricValueKind.FINITE, MetricCoverage.INCOMPLETE,
				MetricComputationStatus.SUCCESS,
				TraversalOutcome.STOPPED_AT_BOUNDARY).scalarAdmissible());
		assertFalse(rich(MetricValueKind.ABSENT, MetricCoverage.INCOMPLETE,
				MetricComputationStatus.UNSUPPORTED, null).scalarAdmissible());
		assertFalse(rich(MetricValueKind.ABSENT, MetricCoverage.INCOMPLETE,
				MetricComputationStatus.INVALID_QUERY,
				TraversalOutcome.TARGET_NOT_REACHABLE).scalarAdmissible());
		assertFalse(rich(MetricValueKind.FINITE, MetricCoverage.INCOMPLETE,
				MetricComputationStatus.SUCCESS,
				TraversalOutcome.DISCONTINUITY_ENCOUNTERED).scalarAdmissible());
		assertFalse(rich(MetricValueKind.ABSENT, MetricCoverage.INCOMPLETE,
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED, null)
				.scalarAdmissible());
		assertFalse(rich(MetricValueKind.POSITIVE_INFINITY,
				MetricCoverage.COMPLETE, MetricComputationStatus.SUCCESS, null)
				.scalarAdmissible());
		assertFalse(rich(MetricValueKind.FINITE, MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, null,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED).scalarAdmissible());
		assertTrue(rich(MetricValueKind.ABSENT, MetricCoverage.INCOMPLETE,
				MetricComputationStatus.NUMERICAL_FAILURE, null).richDefined());
		assertTrue(rich(MetricValueKind.FINITE, MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, null).traversalOutcome().isEmpty());
	}

	private LocusMetricRoute2D resolve(BranchRevision branch, double from,
			double to, Direction direction, BoundaryPolicy boundaryPolicy,
			SamePositionPolicy samePositionPolicy) {
		return resolver.resolve(
				new G7AMetricSemanticModel.BetweenPositionsMetricQuery(
						bind(branch, position(from)), bind(branch, position(to)),
						direction, boundaryPolicy, samePositionPolicy), branch);
	}

	private static MetricPositionBinding2D bind(BranchRevision branch,
			LocusSemanticPosition2D position) {
		return G7AMetricSemanticModel.bind(position, branch);
	}

	private static double routeLength(LocusMetricRoute2D route) {
		return route.orderedRouteSegments().stream()
				.mapToDouble(segment -> segment.straightFixtureLength()).sum();
	}

	private static LocusSemanticPosition2D position(double parameter) {
		return new LocusSemanticPosition2D(LOCUS, BRANCH, PROVIDER, parameter);
	}

	private static G7AMetricSemanticModel.Point crossing(double parameter) {
		double x = parameter * parameter - 1;
		double y = x == 0 ? 0 : parameter * x;
		return new G7AMetricSemanticModel.Point(x, y);
	}

	private static BranchRevision branch(long revision, boolean periodic,
			ValidComponent... components) {
		return new BranchRevision(LOCUS, revision, BRANCH, periodic,
				List.of(components));
	}

	private static ValidComponent component(String key, double start,
			double end) {
		return new ValidComponent(key, start, end, true, true);
	}

	private static MetricContribution2D finite(String branch, String component,
			double value) {
		return new MetricContribution2D(branch, component,
				new FiniteMetricValue2D(value), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.certified(0,
						value > 0 ? OptionalDouble.of(0) : OptionalDouble.empty(),
						"analytic fixture", "fixture certificate"),
				"analytic fixture",
				List.of());
	}

	private static MetricContribution2D estimated(String branch,
			String component, double value, double error) {
		return new MetricContribution2D(branch, component,
				new FiniteMetricValue2D(value), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.estimated(error,
						value > 0 ? OptionalDouble.of(error / value)
								: OptionalDouble.empty(),
						"independent quadrature fixture",
						List.of("smooth speed fixture")),
				"independent quadrature fixture", List.of());
	}

	private static LocusMetricResult2D rich(MetricValueKind valueKind,
			MetricCoverage coverage, MetricComputationStatus status,
			TraversalOutcome outcome) {
		return rich(valueKind, coverage, status, outcome,
				NumericGuarantee.CERTIFIED_ERROR_BOUND);
	}

	private static LocusMetricResult2D rich(MetricValueKind valueKind,
			MetricCoverage coverage, MetricComputationStatus status,
			TraversalOutcome outcome, NumericGuarantee guarantee) {
		MetricValue2D value = valueKind == MetricValueKind.FINITE
				? new FiniteMetricValue2D(2)
				: valueKind == MetricValueKind.POSITIVE_INFINITY
						? new PositiveInfinityMetricValue2D()
						: new AbsentMetricValue2D();
		MetricErrorEvidence2D error = valueKind != MetricValueKind.FINITE
				? MetricErrorEvidence2D.notApplicable("non-finite scalar fixture")
				: guarantee == NumericGuarantee.FLOATING_POINT_UNCERTIFIED
						? MetricErrorEvidence2D.uncertified(
								"uncertified scalar fixture", List.of())
						: MetricErrorEvidence2D.certified(0,
								OptionalDouble.of(0), "scalar fixture",
								"fixture certificate");
		return new LocusMetricResult2D(value, coverage, status,
				MetricRectifiability.RECTIFIABLE, Optional.ofNullable(outcome),
				ConstructionFidelity.EXACT_CONSTRUCTION,
				EvaluatorMethod.ANALYTIC, MetricMethod.CLOSED_FORM,
				RepresentationRole.SEMANTIC_METRIC,
				error,
				"construction-unit", "scalar matrix fixture", List.of(),
				List.of());
	}
}

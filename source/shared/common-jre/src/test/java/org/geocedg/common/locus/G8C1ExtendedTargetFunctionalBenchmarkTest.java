/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionWorkBudget2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.junit.jupiter.api.Test;

/** Deterministic G8C1 functional-work and retained-state gates. */
class G8C1ExtendedTargetFunctionalBenchmarkTest extends BaseUnitTest {

	@Test
	void oneTenAndHundredCompatibleQueriesRemainQueryLocalAndBounded() {
		for (int count : List.of(1, 10, 100)) {
			for (int index = 0; index < count; index++) {
				var fixture = G8C1IntersectionTestSupport.horizontal(
						getConstruction(), "query-" + count + "-" + index,
						0, 3, 0);
				LocusIntersectionResult2D result = G8C1IntersectionTestSupport
						.result(getConstruction(), fixture,
								add("x^2/4+y^2=1"),
								"query-" + count + "-" + index);
				assertBounded(result.getWork());
				assertEquals(1, result.getFiniteSolutions().size());
			}
		}
	}

	@Test
	void oneHundredPointConsumersDoNotRecomputeOrRetainIntersectionState() {
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"consumers", 0, 3, 0);
		var intersection = G8C1IntersectionTestSupport.algorithm(
				getConstruction(), fixture, add("x^2/4+y^2=1"),
				"consumers");
		LocusIntersectionResult2D result = intersection.getResult()
				.getIntersectionResult();
		String token = result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken();
		long semanticEvaluations = result.getWork().getSemanticEvaluations();
		ArrayList<AlgoLocusIntersectionPointV2> consumers = new ArrayList<>();
		for (int index = 0; index < 100; index++) {
			consumers.add(new AlgoLocusIntersectionPointV2(getConstruction(),
					intersection.getResult(), token));
		}
		assertTrue(consumers.stream().allMatch(
				consumer -> consumer.getPoint().isDefined()));
		assertEquals(semanticEvaluations, intersection.getResult()
				.getIntersectionResult().getWork().getSemanticEvaluations());
		assertEquals(0, result.getWork().getRetainedIndexEntries());
	}

	@Test
	void repeatedRunsHaveDeterministicGeometryAndFunctionalCounters() {
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"deterministic", -3, 3, 0);
		GeoConic target = add("x^2/4-y^2=1");
		LocusIntersectionResult2D first = G8C1IntersectionTestSupport.result(
				getConstruction(), fixture, target, "deterministic-one");
		LocusIntersectionResult2D second = G8C1IntersectionTestSupport.result(
				getConstruction(), fixture, target, "deterministic-two");
		assertEquals(G8C1IntersectionTestSupport.parameters(first),
				G8C1IntersectionTestSupport.parameters(second));
		assertSameWork(first.getWork(), second.getWork());
	}

	@Test
	void invalidFunctionRegionsAreBoundedBarriersWithNoForbiddenAuthority() {
		GeoFunction function = add("f(x)=1/x");
		assertTrue(function.setInterval(-1, 1));
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"invalid-budget", -1, 1, 0);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), fixture, function, "invalid-budget");
		assertTrue(result.getWork().getInvalidTargetEvaluations() > 0);
		assertBounded(result.getWork());
		assertTrue(result.getWork().hasZeroForbiddenAuthorityReads());
	}

	@Test
	void semanticReparameterizationPreservesGeometryAndResidualEvidence() {
		var forward = G8C1IntersectionTestSupport.curve(getConstruction(),
				"parameter-forward", 0, 3,
				(source, branch, parameter) -> new LocusPoint2D(parameter, 0));
		var reverse = G8C1IntersectionTestSupport.curve(getConstruction(),
				"parameter-reverse", 0, 3,
				(source, branch, parameter) ->
						new LocusPoint2D(3 - parameter, 0));
		GeoConic target = add("x^2/4+y^2=1");
		LocusIntersectionResult2D first = G8C1IntersectionTestSupport.result(
				getConstruction(), forward, target, "parameter-forward");
		LocusIntersectionResult2D second = G8C1IntersectionTestSupport.result(
				getConstruction(), reverse, target, "parameter-reverse");
		LocusPoint2D firstPoint = first.getFiniteSolutions().get(0)
				.getEvaluatedPoint();
		LocusPoint2D secondPoint = second.getFiniteSolutions().get(0)
				.getEvaluatedPoint();
		assertEquals(firstPoint.getX(), secondPoint.getX(), 2E-11);
		assertEquals(firstPoint.getY(), secondPoint.getY(), 2E-11);
		assertEquals(Math.abs(first.getFiniteSolutions().get(0)
				.getRevisionEvidence().getResidualEvidence()
				.getNormalizedResidual()), Math.abs(second.getFiniteSolutions().get(0)
						.getRevisionEvidence().getResidualEvidence()
						.getNormalizedResidual()), 2E-12);
	}

	@Test
	void representativeUniqueEllipseCounterSnapshotIsVersioned() {
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"counter-snapshot", 0, 3, 0);
		LocusIntersectionInstrumentationSnapshot2D work =
				G8C1IntersectionTestSupport.result(getConstruction(), fixture,
						add("x^2/4+y^2=1"), "counter-snapshot").getWork();
		String actual = String.join(",",
				Long.toString(work.getSemanticEvaluations()),
				Long.toString(work.getDerivativeEvaluations()),
				Long.toString(work.getTargetEvaluations()),
				Long.toString(work.getTargetDerivativeEvaluations()),
				Long.toString(work.getTargetDomainEvaluations()),
				Long.toString(work.getInvalidTargetEvaluations()),
				Long.toString(work.getCandidateIntervals()),
				Long.toString(work.getIsolationSubdivisions()),
				Long.toString(work.getRefinementCalls()),
				Long.toString(work.getRefinementIterations()),
				Long.toString(work.getResidualVerifications()),
				Long.toString(work.getMembershipChecks()),
				Long.toString(work.getDeduplicationComparisons()),
				Long.toString(work.getContinuationComparisons()),
				Long.toString(work.getVerifiedSolutions()),
				Long.toString(work.getRetainedIndexEntries()));
		assertEquals("414,1,411,1,1,1,2,256,2,95,1,1,1,0,1,0",
				actual);
	}

	private static void assertBounded(
			LocusIntersectionInstrumentationSnapshot2D work) {
		assertTrue(work.getSemanticEvaluations()
				<= LocusIntersectionWorkBudget2D.DEFAULT_MAXIMUM_SEMANTIC_EVALUATIONS);
		assertTrue(work.getDerivativeEvaluations()
				<= LocusIntersectionWorkBudget2D.DEFAULT_MAXIMUM_DERIVATIVE_EVALUATIONS);
		assertTrue(work.getTargetEvaluations()
				<= LocusIntersectionWorkBudget2D.DEFAULT_MAXIMUM_TARGET_EVALUATIONS);
		assertTrue(work.getIsolationSubdivisions()
				<= LocusIntersectionWorkBudget2D.DEFAULT_MAXIMUM_ISOLATION_SUBDIVISIONS);
		assertTrue(work.getRefinementIterations()
				<= work.getRefinementCalls()
						* LocusIntersectionWorkBudget2D
								.DEFAULT_MAXIMUM_REFINEMENT_ITERATIONS);
		assertEquals(0, work.getRetainedIndexEntries());
		assertTrue(work.hasZeroForbiddenAuthorityReads());
	}

	private static void assertSameWork(
			LocusIntersectionInstrumentationSnapshot2D first,
			LocusIntersectionInstrumentationSnapshot2D second) {
		assertEquals(first.getSemanticEvaluations(),
				second.getSemanticEvaluations());
		assertEquals(first.getDerivativeEvaluations(),
				second.getDerivativeEvaluations());
		assertEquals(first.getTargetEvaluations(), second.getTargetEvaluations());
		assertEquals(first.getTargetDerivativeEvaluations(),
				second.getTargetDerivativeEvaluations());
		assertEquals(first.getTargetDomainEvaluations(),
				second.getTargetDomainEvaluations());
		assertEquals(first.getIsolationSubdivisions(),
				second.getIsolationSubdivisions());
		assertEquals(first.getRefinementIterations(),
				second.getRefinementIterations());
	}
}

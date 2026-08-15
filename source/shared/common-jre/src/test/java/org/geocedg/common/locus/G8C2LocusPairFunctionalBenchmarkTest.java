/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionWorkBudget2D;
import org.geocedg.common.locus.G8BIntersectionFixtures.Fixture;
import org.geogebra.common.BaseUnitTest;
import org.junit.jupiter.api.Test;

/** Deterministic G8C2 pair-work and retained-state gates. */
class G8C2LocusPairFunctionalBenchmarkTest extends BaseUnitTest {

	@Test
	void oneTenAndHundredCompatibleQueriesRemainQueryLocalAndBounded() {
		for (int count : List.of(1, 10, 100)) {
			for (int index = 0; index < count; index++) {
				Fixture first = G8C2IntersectionTestSupport.line(getConstruction(),
						"query-A-" + count + "-" + index, -1, 1, 0, true);
				Fixture second = G8C2IntersectionTestSupport.line(getConstruction(),
						"query-B-" + count + "-" + index, -1, 1, 0, false);
				LocusIntersectionResult2D result = G8C2IntersectionTestSupport
						.result(getConstruction(), first, second,
								"query-" + count + "-" + index,
								analyticCrossing());
				assertEquals(1, result.getFiniteSolutions().size());
				assertBounded(result.getWork());
			}
		}
	}

	@Test
	void oneHundredPointConsumersDoNotRecomputeOrRetainPairState() {
		Fixture first = G8C2IntersectionTestSupport.line(getConstruction(),
				"consumers-A", -1, 1, 0, true);
		Fixture second = G8C2IntersectionTestSupport.line(getConstruction(),
				"consumers-B", -1, 1, 0, false);
		var intersection = G8C2IntersectionTestSupport.algorithm(
				getConstruction(), first, second, "consumers", analyticCrossing(),
				new org.geogebra.common.kernel.geos.GeoElement[0]);
		LocusIntersectionResult2D result = intersection.getResult()
				.getIntersectionResult();
		String token = result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken();
		long evaluations = result.getWork().getSemanticEvaluations();
		ArrayList<AlgoLocusIntersectionPointV2> consumers = new ArrayList<>();
		for (int index = 0; index < 100; index++) {
			consumers.add(new AlgoLocusIntersectionPointV2(getConstruction(),
					intersection.getResult(), token));
		}
		assertTrue(consumers.stream()
				.allMatch(consumer -> consumer.getPoint().isDefined()));
		assertEquals(evaluations, intersection.getResult()
				.getIntersectionResult().getWork().getSemanticEvaluations());
		assertEquals(0, result.getWork().getRetainedPairEntries());
		assertEquals(0, result.getWork().getRetainedIndexEntries());
	}

	@Test
	void repeatedRunsHaveDeterministicGeometryAndPairCounters() {
		Fixture first = G8C2IntersectionTestSupport.line(getConstruction(),
				"deterministic-A", -1, 1, 0, true);
		Fixture second = G8C2IntersectionTestSupport.line(getConstruction(),
				"deterministic-B", -1, 1, 0, false);
		LocusIntersectionResult2D firstResult = G8C2IntersectionTestSupport
				.result(getConstruction(), first, second, "deterministic-one",
						analyticCrossing());
		LocusIntersectionResult2D secondResult = G8C2IntersectionTestSupport
				.result(getConstruction(), first, second, "deterministic-two",
						analyticCrossing());
		assertEquals(firstResult.getFiniteSolutions().get(0).getEvaluatedPoint(),
				secondResult.getFiniteSolutions().get(0).getEvaluatedPoint());
		assertSamePairWork(firstResult.getWork(), secondResult.getWork());
	}

	@Test
	void representativeEvaluatorCounterSnapshotIsVersioned() {
		Fixture first = G8C2IntersectionTestSupport.line(getConstruction(),
				"counter-A", -1, 1, 0, true);
		Fixture second = G8C2IntersectionTestSupport.line(getConstruction(),
				"counter-B", -1, 1, 0, false);
		LocusIntersectionInstrumentationSnapshot2D work =
				G8C2IntersectionTestSupport.algorithm(getConstruction(), first,
						second, "counter").getResult().getIntersectionResult()
						.getWork();
		String actual = String.join(",",
				Long.toString(work.getSemanticEvaluations()),
				Long.toString(work.getDerivativeEvaluations()),
				Long.toString(work.getResidualVerifications()),
				Long.toString(work.getVerifiedSolutions()),
				Long.toString(work.getBranchPairs()),
				Long.toString(work.getComponentPairs()),
				Long.toString(work.getParameterBoxesVisited()),
				Long.toString(work.getParameterBoxesRejected()),
				Long.toString(work.getPairCandidateBoxes()),
				Long.toString(work.getPairRefinementCalls()),
				Long.toString(work.getPairRefinementIterations()),
				Long.toString(work.getJacobianEvaluations()),
				Long.toString(work.getOverlapChecks()),
				Long.toString(work.getPairContinuationComparisons()),
				Long.toString(work.getRetainedPairEntries()));
		assertEquals("120,18,1,1,1,1,1024,1020,4,4,8,9,1,0,0",
				actual);
	}

	@Test
	void componentPairProductIsCountedAndBounded() {
		Fixture first = components("component-A", 2, true);
		Fixture second = components("component-B", 3, false);
		LocusIntersectionResult2D result = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "component-product",
				G8C2IntersectionTestSupport.capability("empty/v1",
						G8C2IntersectionTestSupport::completeEmpty));
		assertEquals(1, result.getWork().getBranchPairs());
		assertEquals(6, result.getWork().getComponentPairs());
		assertBounded(result.getWork());
	}

	@Test
	void excessiveComponentProductFailsAtomicallyAtVersionedBudget() {
		Fixture first = components("explosion-A", 33, true);
		Fixture second = components("explosion-B", 33, false);
		LocusIntersectionResult2D result = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "component-explosion",
				G8C2IntersectionTestSupport.capability("unused/v1",
						G8C2IntersectionTestSupport::completeEmpty));
		assertEquals(ComputationStatus.WORK_LIMIT_REACHED,
				result.getComputationStatus());
		assertTrue(result.getFiniteSolutions().isEmpty());
		assertTrue(result.getWork().hasZeroForbiddenAuthorityReads());
	}

	@Test
	void reparameterizationDoesNotChangePairTokenOrGeometry() {
		Fixture forward = G8C2IntersectionTestSupport.curve(getConstruction(),
				"reparam-A", 0, 2, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter, 0));
		Fixture reverse = G8C2IntersectionTestSupport.curve(getConstruction(),
				"reparam-A", 0, 2, false,
				(source, branch, parameter) -> new LocusPoint2D(2 - parameter, 0));
		Fixture target = G8C2IntersectionTestSupport.curve(getConstruction(),
				"reparam-B", -1, 1, false,
				(source, branch, parameter) -> new LocusPoint2D(0.5, parameter));
		var forwardCapability = G8C2IntersectionTestSupport.capability(
				"forward/v1", context -> G8C2IntersectionTestSupport.roots(context,
						List.of(G8C2IntersectionTestSupport.simple(0.5, 0,
								"same-root")), Completeness.COMPLETE));
		var reverseCapability = G8C2IntersectionTestSupport.capability(
				"reverse/v1", context -> G8C2IntersectionTestSupport.roots(context,
						List.of(G8C2IntersectionTestSupport.simple(1.5, 0,
								"same-root")), Completeness.COMPLETE));
		LocusIntersectionResult2D first = G8C2IntersectionTestSupport.result(
				getConstruction(), forward, target, "reparameterization",
				forwardCapability);
		LocusIntersectionResult2D second = G8C2IntersectionTestSupport.result(
				getConstruction(), reverse, target, "reparameterization",
				reverseCapability);
		assertEquals(first.getFiniteSolutions().get(0).getIdentity().getRootToken(),
				second.getFiniteSolutions().get(0).getIdentity().getRootToken());
		assertEquals(first.getFiniteSolutions().get(0).getEvaluatedPoint(),
				second.getFiniteSolutions().get(0).getEvaluatedPoint());
	}

	@Test
	void scaleAndTranslationPreserveVerifiedPairSemantics() {
		Fixture baseA = G8C2IntersectionTestSupport.line(getConstruction(),
				"scale-base-A", -1, 1, 0, true);
		Fixture baseB = G8C2IntersectionTestSupport.line(getConstruction(),
				"scale-base-B", -1, 1, 0, false);
		Fixture movedA = G8C2IntersectionTestSupport.curve(getConstruction(),
				"scale-moved-A", -1, 1, false,
				(source, branch, parameter) ->
						new LocusPoint2D(3 * parameter + 10, -5));
		Fixture movedB = G8C2IntersectionTestSupport.curve(getConstruction(),
				"scale-moved-B", -1, 1, false,
				(source, branch, parameter) ->
						new LocusPoint2D(10, 2 * parameter - 5));
		LocusIntersectionResult2D base = G8C2IntersectionTestSupport.result(
				getConstruction(), baseA, baseB, "scale-base", analyticCrossing());
		LocusIntersectionResult2D moved = G8C2IntersectionTestSupport.result(
				getConstruction(), movedA, movedB, "scale-moved",
				analyticCrossing());
		assertEquals(0, pairResidual(base), 0);
		assertEquals(0, pairResidual(moved), 0);
		assertEquals(10, moved.getFiniteSolutions().get(0).getEvaluatedPoint()
				.getX(), 0);
		assertEquals(-5, moved.getFiniteSolutions().get(0).getEvaluatedPoint()
				.getY(), 0);
	}

	private org.geocedg.common.kernel.locus.intersection
			.LocusPairIntersectionCapability2D analyticCrossing() {
		return G8C2IntersectionTestSupport.capability("analytic-cross/v1",
				context -> G8C2IntersectionTestSupport.roots(context,
						List.of(G8C2IntersectionTestSupport.simple(0, 0,
								"cross")), Completeness.COMPLETE));
	}

	private Fixture components(String identity, int count, boolean horizontal) {
		ExplicitNumericDomainProvider2D provider = G8BIntersectionFixtures
				.provider(identity, 0, count * 2.0, true, true, false,
						org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
								.Orientation.INCREASING);
		ArrayList<LocusInterval2D> components = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			components.add(new LocusInterval2D(index * 2.0, index * 2.0 + 1,
					true, true));
		}
		LocusBranch2D branch = G8BIntersectionFixtures.branch(identity + "/branch",
				provider, components, EnumSet.of(BranchProperty.FINITE));
		return G8BIntersectionFixtures.create(getConstruction(), identity,
				provider, List.of(branch), (source, semanticBranch, parameter) ->
						horizontal ? new LocusPoint2D(parameter, 0)
								: new LocusPoint2D(0, parameter));
	}

	private static double pairResidual(LocusIntersectionResult2D result) {
		return result.getFiniteSolutions().get(0).getPairEvidence().orElseThrow()
				.getResidual().getModelCoordinateResidual();
	}

	private static void assertBounded(
			LocusIntersectionInstrumentationSnapshot2D work) {
		assertTrue(work.getBranchPairs()
				<= LocusPairIntersectionWorkBudget2D.DEFAULT_MAXIMUM_BRANCH_PAIRS);
		assertTrue(work.getComponentPairs()
				<= LocusPairIntersectionWorkBudget2D.DEFAULT_MAXIMUM_COMPONENT_PAIRS);
		assertTrue(work.getParameterBoxesVisited()
				<= LocusPairIntersectionWorkBudget2D.DEFAULT_MAXIMUM_PARAMETER_BOXES);
		assertTrue(work.getPairCandidateBoxes()
				<= LocusPairIntersectionWorkBudget2D.DEFAULT_MAXIMUM_CANDIDATE_BOXES);
		assertTrue(work.getPairRefinementCalls()
				<= LocusPairIntersectionWorkBudget2D.DEFAULT_MAXIMUM_PAIR_REFINEMENTS);
		assertTrue(work.getJacobianEvaluations()
				<= LocusPairIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_JACOBIAN_EVALUATIONS);
		assertTrue(work.getOverlapChecks()
				<= LocusPairIntersectionWorkBudget2D.DEFAULT_MAXIMUM_OVERLAP_CHECKS);
		assertEquals(0, work.getRetainedPairEntries());
		assertEquals(0, work.getRetainedIndexEntries());
		assertTrue(work.hasZeroForbiddenAuthorityReads());
	}

	private static void assertSamePairWork(
			LocusIntersectionInstrumentationSnapshot2D first,
			LocusIntersectionInstrumentationSnapshot2D second) {
		assertEquals(first.getSemanticEvaluations(),
				second.getSemanticEvaluations());
		assertEquals(first.getDerivativeEvaluations(),
				second.getDerivativeEvaluations());
		assertEquals(first.getResidualVerifications(),
				second.getResidualVerifications());
		assertEquals(first.getBranchPairs(), second.getBranchPairs());
		assertEquals(first.getComponentPairs(), second.getComponentPairs());
		assertEquals(first.getParameterBoxesVisited(),
				second.getParameterBoxesVisited());
		assertEquals(first.getParameterBoxesRejected(),
				second.getParameterBoxesRejected());
		assertEquals(first.getPairCandidateBoxes(),
				second.getPairCandidateBoxes());
		assertEquals(first.getPairRefinementIterations(),
				second.getPairRefinementIterations());
	}
}

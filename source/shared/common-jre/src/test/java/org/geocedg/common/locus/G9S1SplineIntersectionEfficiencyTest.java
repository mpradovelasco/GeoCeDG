/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionWorkBudget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionWorkLimitException;
import org.junit.jupiter.api.Test;

/** Functional work evidence for the G9S1 span-polynomial target capability. */
final class G9S1SplineIntersectionEfficiencyTest
		extends G9U0PublicSurfaceTestBase {

	@Test
	void explicitSpanCountersAreDeterministicAndBounded() {
		createEightSpanLineSpline();
		add("target:x=0.25");

		GeoLocusIntersectionResult first = add("R1=Intersect(S,target)");
		GeoLocusIntersectionResult second = add("R2=Intersect(S,target)");
		LocusIntersectionInstrumentationSnapshot2D firstWork =
				first.getIntersectionResult().getWork();
		LocusIntersectionInstrumentationSnapshot2D secondWork =
				second.getIntersectionResult().getWork();

		assertEquals(8, firstWork.getPolynomialSpansExamined());
		assertEquals(7, firstWork.getPolynomialSpansRejected());
		assertEquals(1, firstWork.getPolynomialRootCandidates());
		assertEquals(1, firstWork.getVerifiedSolutions());
		assertEquals(firstWork.getPolynomialSpansExamined(),
				secondWork.getPolynomialSpansExamined());
		assertEquals(firstWork.getPolynomialSpansRejected(),
				secondWork.getPolynomialSpansRejected());
		assertEquals(firstWork.getPolynomialRootCandidates(),
				secondWork.getPolynomialRootCandidates());
		assertEquals(firstWork.getCandidateIntervals(),
				secondWork.getCandidateIntervals());
		assertEquals(firstWork.getIsolationSubdivisions(),
				secondWork.getIsolationSubdivisions());
		assertEquals(firstWork.getRefinementCalls(),
				secondWork.getRefinementCalls());
		assertEquals(firstWork.getRefinementIterations(),
				secondWork.getRefinementIterations());
		assertTrue(firstWork.getCandidateIntervals() > 0);
		assertEquals(0, firstWork.getRefinementCalls());
		assertEquals(0, firstWork.getRefinementIterations());
		assertTrue(firstWork.getPolynomialSpansExamined()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_ISOLATION_SUBDIVISIONS);
		assertTrue(firstWork.getPolynomialRootCandidates()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_CANDIDATE_INTERVALS);
		assertTrue(firstWork.getCandidateIntervals()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_CANDIDATE_INTERVALS);
		assertTrue(firstWork.getIsolationSubdivisions()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_ISOLATION_SUBDIVISIONS);
		assertTrue(firstWork.getRefinementIterations()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_REFINEMENT_ITERATIONS);
		assertTrue(firstWork.hasZeroForbiddenAuthorityReads());
	}

	@Test
	void noRootSpansAreCountedWithoutFabricatingCandidates() {
		createEightSpanLineSpline();
		add("target:y=100");

		GeoLocusIntersectionResult result = add("R=Intersect(S,target)");
		LocusIntersectionInstrumentationSnapshot2D work =
				result.getIntersectionResult().getWork();

		assertEquals(8, work.getPolynomialSpansExamined());
		assertEquals(8, work.getPolynomialSpansRejected());
		assertEquals(0, work.getPolynomialRootCandidates());
		assertEquals(0, work.getVerifiedSolutions());
		assertEquals(0, result.getIntersectionResult().getFiniteSolutions().size());
	}

	@Test
	void spanAndRawRootCountersEnforceExistingWorkCeilings() {
		LocusIntersectionInstrumentation2D work =
				new LocusIntersectionInstrumentation2D(
						new LocusIntersectionWorkBudget2D(10, 10, 10, 1, 1,
								10, 10, 10, 10, 10, 10, 0, 2));

		work.recordPolynomialSpanExamined();
		work.recordPolynomialRootCandidate();
		assertThrows(LocusIntersectionWorkLimitException.class,
				work::recordPolynomialSpanExamined);
		assertThrows(LocusIntersectionWorkLimitException.class,
				work::recordPolynomialRootCandidate);
	}

	private void createEightSpanLineSpline() {
		StringBuilder points = new StringBuilder();
		for (int index = 0; index < 9; index++) {
			String label = "P" + index;
			add(label + "=(" + (index - 4) + ",0)");
			if (index > 0) {
				points.append(',');
			}
			points.append(label);
		}
		add("S=SplineV2({" + points + "},3)");
	}
}

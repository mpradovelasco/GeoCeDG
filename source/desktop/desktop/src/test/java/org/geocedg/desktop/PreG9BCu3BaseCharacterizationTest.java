/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.IntersectionClassification2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-CU3 step T1 characterization pins on synthetic constructions (owner CU3, reference
 * evidence of the base {@code 80d72a50}, not normative): the findings H-CU3-1 to H-CU3-4 of the
 * preparation ({@code docs/architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md}
 * section 4). The current engine accepts and classifies roots with unenclosed binary64 values:
 * it declares a tangency where the absence of intersection is certifiable, publishes noise roots
 * and duplicated preimages around a near-tangency, and classifies an endpoint tangency as a
 * transverse root. The pins of B08/B09, H02 and H06 stay in the {@code CU0} tests. A later step
 * inverts these pins only with a test that proves the correction (decision C1-4 of {@code CU0}):
 * increment E1 of {@code CU3} inverts H-CU3-1, H-CU3-3 and H-CU3-4 with the root-count
 * certificates of {@code cu3-certified-admission/v1} ({@code PreG9BCu3CertifiedEvidenceTest});
 * H-CU3-2 needs the exact decision at the included end and stays pinned until increment E3.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3BaseCharacterizationTest {

	/**
	 * H-CU3-3: the circle of radius 1.5 against the line {@code y = 1.5 + 1e-13}, which misses
	 * it, and against the circle of centre {@code (3 + 1e-13, 0)} and radius 1.5, which is
	 * disjoint from it. The base published one tangent solution in each case; a certified fixed
	 * sign of the residual now withdraws it.
	 */
	@Test
	void absentIntersectionIsCertifiedAbsent() throws Exception {
		for (String target : new String[] {"g=Line((0,1.5000000000001),(1,1.5000000000001))",
				"g=Circle((3.0000000000001,0),1.5)"}) {
			List<String[]> solutions = PreG9BR6PlusF3Fixtures.onEdt(() -> solutions(target));
			assertEquals(0, solutions.size(), inverted("H-CU3-3", target
					+ ": no solution where the absence of intersection is certified"));
		}
	}

	/**
	 * H-CU3-1 and H-CU3-4: against {@code y = 1.5 - 1e-13} and {@code y = 1.5 - 1e-10} the true
	 * intersection is two transverse roots near {@code pi / 2}. The base published four and
	 * three solutions, among them a duplicate of a true root and, for {@code 1e-13}, a tangent
	 * solution at the critical point; the certificate of two roots around the certified
	 * critical point now leaves exactly the two true roots.
	 */
	@Test
	void nearTangencyPublishesExactlyTheTwoTrueRoots() throws Exception {
		for (String target : new String[] {"g=Line((0,1.4999999999999),(1,1.4999999999999))",
				"g=Line((0,1.4999999999),(1,1.4999999999))"}) {
			List<String[]> solutions = PreG9BR6PlusF3Fixtures.onEdt(() -> solutions(target));
			assertEquals(2, solutions.size(), inverted("H-CU3-1/H-CU3-4",
					target + ": two solutions for two true roots"));
			assertEquals(2, admissible(solutions), target + ": both are admissible");
			assertEquals(2, count(solutions, "TRANSVERSE_ESTABLISHED"),
					inverted("H-CU3-1", target + ": no tangent solution at the critical point"));
		}
	}

	/**
	 * H-CU3-2: the quarter arc of the unit circle from {@code (1, 0)} touches {@code x = 1} at its
	 * included start with a double zero of the residual; the engine publishes an admissible
	 * transverse root at the included endpoint.
	 */
	@Test
	void endpointTangencyIsClassifiedTransverse() throws Exception {
		List<String[]> solutions = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			for (String command : new String[] {"a=CircularArc((0,0),(1,0),(0,1))",
					"P=Point(a)", "Q=Dilate(P,1,(0,0))", "L=LocusV2(Q,P)",
					"g=Line((1,0),(1,1))"}) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			return read(app);
		});
		assertEquals(1, solutions.size(), "one solution at the included start");
		assertEquals("TRANSVERSE_ESTABLISHED", solutions.get(0)[0],
				pin("H-CU3-2", "the endpoint tangency is classified transverse"));
		assertEquals("INCLUDED_ENDPOINT", solutions.get(0)[1]);
		assertEquals("true", solutions.get(0)[2],
				pin("H-CU3-2", "the misclassified root is admissible"));
	}

	/** @return contact class, domain location and admissibility of every finite solution */
	private static List<String[]> solutions(String target) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"O=(0,0)", "c=Circle(O,1)", "P=Point(c)",
				"Q=Dilate(P,1.5,O)", "L=LocusV2(Q,P)", target}) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		return read(app);
	}

	private static List<String[]> read(AppGeoCeDG app) {
		GeoElement geo = PreG9BR6PlusF3Fixtures.q(app, "R=Intersect(L,g)");
		assertTrue(geo instanceof GeoLocusIntersectionResult, "the rich result is created");
		GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) geo;
		List<String[]> rows = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			IntersectionClassification2D classification = solution.getClassification();
			rows.add(new String[] {classification.getContactClass().name(),
					classification.getDomainLocation().name(),
					String.valueOf(result.isPointAdmissible(solution.getIdentity()
							.getRootToken()))});
		}
		return rows;
	}

	private static int admissible(List<String[]> solutions) {
		int count = 0;
		for (String[] solution : solutions) {
			count += "true".equals(solution[2]) ? 1 : 0;
		}
		return count;
	}

	private static int count(List<String[]> solutions, String contactClass) {
		int count = 0;
		for (String[] solution : solutions) {
			count += contactClass.equals(solution[0]) ? 1 : 0;
		}
		return count;
	}

	private static String pin(String finding, String detail) {
		return PreG9BCu0Baseline.pinMessage(finding, "CU3", detail);
	}

	private static String inverted(String finding, String detail) {
		return PreG9BCu0Baseline.inversionMessage(finding, "CU3", detail);
	}
}

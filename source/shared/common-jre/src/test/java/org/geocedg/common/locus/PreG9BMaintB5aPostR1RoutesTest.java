/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter.richResultOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a, no regression on the post-R1 batteries ({@code locus-v2-metrics.md}
 * section 28.3.1, validation): every conditioned route of the shared post-R1 batteries that
 * {@code conditioned-length-scalar/v1} admitted is still a number under version 2, with the
 * value of the baseline measured before the change (bit for bit: the check never changes the
 * estimate) and the evaluations of the check recorded. The baseline is the scratch
 * measurement of the candidate report, taken on {@code 228580e96} with the same routes.
 */
final class PreG9BMaintB5aPostR1RoutesTest extends G9U0PublicSurfaceTestBase {
	private int counter;

	private GeoLocusV2 segmentLocus(String abscissa, String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(" + abscissa + "," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	/**
	 * Measures {@code a -> b} on the locus and checks that it is still the admitted number of
	 * the baseline, checked on {@code leaves} leaves at four evaluations each.
	 */
	private void assertStillAdmitted(String locus, double a, double b, double baseline,
			int leaves) {
		int id = ++counter;
		add("RA" + id + "=Point(" + locus + ",\"" + BRANCH + "\"," + a + ")");
		add("RB" + id + "=Point(" + locus + ",\"" + BRANCH + "\"," + b + ")");
		GeoNumeric number = add("rL" + id + "=LocusLength(" + locus + ",RA" + id + ",RB" + id
				+ ")");
		LocusMetricResult2D result = richResultOf((GeoElement) number).getMetricResult();
		String where = locus + " " + a + " -> " + b + ": " + result.getDiagnostics();
		assertTrue(result.isScalarAdmissible(), where);
		assertTrue(number.isDefined(), where);
		assertEquals(baseline, result.getMetricValue().getFiniteValue().orElseThrow(), 0, where);
		assertEquals(baseline, number.getValue(), 0, where);
		LocusConditionedRouteJumpGuard2D.Record record = result.getConditionedRoute()
				.orElseThrow().getJumpGuard().orElseThrow();
		assertEquals(leaves, record.getLeavesTested(), where);
		assertEquals(0, record.getLeavesBelowResolution(), where);
		assertEquals(4L * leaves, record.getEvaluations(), where);
		assertFalse(result.getDiagnostics().stream().anyMatch(d -> d.getCode().name()
				.startsWith("CHORD_SCALING")), where);
	}

	@Test
	void theRoutesOfTheMetricBatteryStayNumbers() {
		segmentLocus("x(P)", "exp(x(P))");
		assertStillAdmitted("L", 0.2, 0.7, 0.9388979755898462, 64);
	}

	@Test
	void theRoutesBesideBandsStayNumbers() {
		GeoLocusV2 locus = segmentLocus("x(P)+floor(x(P)+0.25)", "exp(x(P))");
		List<LocusInterval2D> regions = locus.getConditionedEvidence(BRANCH).getRegions();
		assertStillAdmitted("L", regions.get(1).getLower(), regions.get(1).getUpper(),
				0.6513965339576451, 64);
		assertStillAdmitted("L", 0, regions.get(0).getUpper(), 1.3520983448323027, 64);
	}

	@Test
	void theRoutesBesideAnAbsenceAndAJumpStayNumbers() {
		segmentLocus("x(P)+floor(x(P)+0.25)", "sqrt(x(P)-0.3)+exp(x(P))");
		assertStillAdmitted("L", 0.5, 0.6, 0.2915902683208643, 64);
	}

	@Test
	void theRouteBeforeATransverseJumpStaysANumber() {
		segmentLocus("x(P)", "exp(x(P))+0.0009*floor(x(P)+0.5)");
		assertStillAdmitted("L", 0.2, 0.45, 0.42785525348468, 64);
	}

	@Test
	void theRouteAwayFromAFoldStaysANumber() {
		segmentLocus("x(P)", "sqrt(x(P)-0.3)+exp(x(P))");
		assertStillAdmitted("L", 0.5, 0.9, 1.2065938688423534, 64);
	}

	@Test
	void theRoutesThatFollowTheSourceStayNumbers() {
		add("k=1");
		segmentLocus("x(P)", "k*exp(x(P))");
		assertStillAdmitted("L", 0.2, 0.7, 0.9388979755898462, 64);
		GeoNumeric factor = (GeoNumeric) lookup("k");
		factor.setValue(2);
		factor.updateCascade();
		LocusMetricResult2D changed = richResultOf(lookup("rL1")).getMetricResult();
		assertTrue(changed.isScalarAdmissible(), changed.getDiagnostics().toString());
		assertEquals(1.6631380213945244, changed.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(4L * 64, changed.getConditionedRoute().orElseThrow().getJumpGuard()
				.orElseThrow().getEvaluations());
	}

	@Test
	void theRoutesAcrossAHoleThatNothingMeetsStayNumbers() {
		segmentLocus("x(P)", "x(P)+0*sqrt((x(P)-0.3)^2-1E-24)+0*exp(x(P))");
		assertStillAdmitted("L", 0.21, 0.6, 0.5515432893255071, 64);
		assertStillAdmitted("L", 0.6, 0.9, 0.4242640687119286, 64);
	}

	@Test
	void theRoutesOfAWholePeriodStayNumbers() {
		add("c=Circle((0,0),1)");
		add("E=Point(c)");
		add("Q=(x(E)*exp(0*y(E)),y(E))");
		add("L=LocusV2(Q,E)");
		// Across the seam: two stretches, checked in increasing parameter order.
		assertStillAdmitted("L", 3, -3, 0.28318530717958645, 128);
		assertStillAdmitted("L", -3, 3, 5.999999999985266, 256);
		assertStillAdmitted("L", -1, 2, 2.999999999992633, 128);
	}

	@Test
	void theRoutesOfAConsumerOfARootStayNumbers() {
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		add("S=LocusV2(M,E)");
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		GeoPoint driver = (GeoPoint) lookup("T");
		driver.setCoords(0.5 + 3 * Math.cos(3.0), 3 * Math.sin(3.0), 1.0);
		driver.updateCascade();
		GeoLocusIntersectionResult rich = add("v=Intersect(S,r)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken() + "\")");
		add("ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = add("L=LocusV2(ZP,T)");
		getKernel().updateConstruction(false);
		List<LocusInterval2D> regions = consumer.getConditionedEvidence(BRANCH).getRegions();
		assertEquals(3, regions.size());
		double[][] expected = {{0.5244195144187664, 64, 1.4183070533009867, 294},
				{0.37025403305268767, 64, 1.4040639859731827, 400},
				{0.5244195144187651, 64, Double.NaN, 0}};
		for (int index = 0; index < 3; index++) {
			double lower = regions.get(index).getLower();
			double upper = regions.get(index).getUpper();
			assertStillAdmitted("L", lower + 0.3 * (upper - lower),
					lower + 0.7 * (upper - lower), expected[index][0], (int) expected[index][1]);
			if (index < 2) {
				assertStillAdmitted("L", lower, upper, expected[index][2],
						(int) expected[index][3]);
			}
		}
	}

	@Test
	void theRouteOfASimilarityImageStaysANumber() {
		segmentLocus("x(P)+0.0009*floor(x(P)+0.3)", "exp(x(P))");
		add("T=Dilate(L,4)");
		assertStillAdmitted("T", 0.8, 0.9, 1.0181721084696636, 64);
	}

	@Test
	void theNonUniformReferencesOfThePublicSurfaceStayNumbers() {
		segmentLocus("x(P)^3", "x(P)");
		assertStillAdmitted("L", 0, 1, 1.547865654674981, 93);
	}

	@Test
	void theRoutesThatTheBatteriesRefusedAreStillRefusedForTheirOwnCauses() {
		segmentLocus("x(P)", "exp(x(P))+0.0009*floor(x(P)+0.5)");
		add("RA=Point(L,\"" + BRANCH + "\",0.2)");
		add("RB=Point(L,\"" + BRANCH + "\",0.7)");
		LocusMetricResult2D across = richResultOf((GeoElement) add("rM=LocusLength(L,RA,RB)"))
				.getMetricResult();
		assertTrue(across.getDiagnostics().stream().anyMatch(d -> d.getCode()
				== MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED), across.getDiagnostics()
				.toString());
		assertFalse(across.getDiagnostics().stream().anyMatch(d -> d.getCode().name()
				.startsWith("CHORD_SCALING")));
		assertFalse(across.isScalarAdmissible());
	}
}

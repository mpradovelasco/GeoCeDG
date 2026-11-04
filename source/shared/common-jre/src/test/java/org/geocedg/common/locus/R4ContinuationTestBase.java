/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Comparator;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.*;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Command-level fixtures; all points are created by the normal offered operation. */
abstract class R4ContinuationTestBase extends G9U0PublicSurfaceTestBase {
	LocusPointInteractionPolicy2D policy = LocusPointInteractionPolicy2D.initial(.5);
	LocusPoint2D at(GeoLocusV2 source, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(100)) {
			var value = source.getSemanticDefinition().evaluate(BRANCH, parameter, session);
			assertTrue(value.isValid()); return value.getPoint();
		}
	}
	GeoPoint offered(GeoLocusV2 source, double parameter, String label) {
		var target = at(source, parameter);
		try (var offer = LocusPointCreation2D.query(source, target.getX(), target.getY(), policy)) {
			assertFalse(offer.getChoices().isEmpty(), offer.getFailure());
			return offer.commit(label, offer.getChoices().stream().min(Comparator.comparingDouble(
					choice -> Math.abs(choice.getAddress().getCanonicalParameter() - parameter))).orElseThrow());
		}
	}
	LocusSemanticAddress2D address(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
	}
	double lifted(GeoPoint point) {
		var address = address(point);
		return address.getCanonicalParameter() + 2 * Math.PI * address.getPeriodicLift();
	}
	LocusPointContinuation2D.Result move(GeoPoint point, LocusPointContinuation2D.Context context,
			double x, double y) {
		double previous = lifted(point);
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, x, y, policy, context);
		assertTrue(result.isEstablished(), () -> result.getDiagnostic() + " work=" + result.getIntervalPasses()
				+ " charts=" + result.getContinuationCharts() + " attempts=" + result.getContinuationChartAttempts()
				+ " subdivisions=" + result.getContinuationSubdivisions());
		assertEquals("LOCAL_STRICT_PROJECTION_CHART", result.getMethod());
		assertTrue(result.getParameterError() <= policy.getParameterTolerance());
		assertTrue(Math.abs(lifted(point) - previous) <= result.getMaximumParameterChange() + 3e-15,
				() -> "actual=" + Math.abs(lifted(point) - previous) + " bound=" + result.getMaximumParameterChange());
		assertTrue(result.getContinuationIntervalEvaluations() <= result.getIntervalPasses());
		assertTrue(result.getIntervalPasses() <= policy.getWorkBudget().getMaximumSubdivisions());
		return result;
	}
	GeoLocusV2 meetCircle() {
		add("c=Circle((0,0),1)"); add("N=Point(c)");
		add("horizontal=Line(N,xAxis)"); add("vertical=Line(N,yAxis)");
		add("Q=Intersect(horizontal,vertical)"); return add("L=LocusV2(Q,N)");
	}
}

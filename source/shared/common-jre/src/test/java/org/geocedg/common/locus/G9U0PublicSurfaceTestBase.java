/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Shared command-level setup for the exact G9U0 focused scenario classes. */
abstract class G9U0PublicSurfaceTestBase extends BaseUnitTest {

	static final String BRANCH = SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	GeoLocusV2 createParabola() {
		return createScalarLocus("L", "s", "Q", "(s,s^2)",
				"{false,{-2,2,true,true}}");
	}

	GeoLocusV2 createLine() {
		return createScalarLocus("L", "s", "Q", "(s,0)",
				"{false,{-2,2,true,true}}");
	}

	/**
	 * The certified unit circle (PRE-G9B-CU2 step T11, declared conformity): a periodic
	 * scalar locus is outside class v3 and has no certified component, so the circle is
	 * driven by a point of a circle (class v2). Its canonical domain is {@code [-pi, pi)},
	 * with the seam at {@code -pi}, and the point of parameter {@code t} is
	 * {@code (cos t, sin t)}.
	 */
	GeoLocusV2 createPeriodicCircle() {
		add("periodicCircle=Circle((0,0),1)");
		add("periodicDriver=Point(periodicCircle)");
		add("Q=periodicDriver+(0,0)");
		GeoLocusV2 locus = add("L=LocusV2(Q,periodicDriver)");
		assertNotNull(locus);
		return locus;
	}

	/**
	 * The certified parabola {@code y = x^2}, {@code -2 <= x <= 2} (PRE-G9B-CU2 step T11,
	 * declared conformity): the scalar parabola {@code (s,s^2)} is outside class v3, so the
	 * same curve is built from its focus {@code (0,1/4)} and its directrix
	 * {@code y = -1/4}, driven by a point of a segment of the directrix (class v1). The
	 * driver parameter is {@code u = (x + 2) / 4} of {@code [0, 1]}.
	 */
	GeoLocusV2 certifiedParabola() {
		return certifiedParabola("L", "Q", "parabola");
	}

	/**
	 * @param locusLabel label of the locus
	 * @param pointLabel label of the traced point
	 * @param prefix prefix of the labels of the construction
	 * @return the certified parabola of {@link #certifiedParabola()} with other labels
	 */
	GeoLocusV2 certifiedParabola(String locusLabel, String pointLabel, String prefix) {
		// A lowercase label of a coordinate literal names a vector: points start uppercase.
		String points = Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);
		add(points + "Focus=(0,1/4)");
		add(prefix + "Line=Line((-2,-1/4),(2,-1/4))");
		add(prefix + "Directrix=Segment((-2,-1/4),(2,-1/4))");
		add(points + "Driver=Point(" + prefix + "Directrix)");
		add(prefix + "Normal=PerpendicularLine(" + points + "Driver," + prefix + "Line)");
		add(prefix + "Chord=Line(" + points + "Focus," + points + "Driver)");
		add(points + "Middle=Midpoint(" + points + "Focus," + points + "Driver)");
		add(prefix + "Bisector=PerpendicularLine(" + points + "Middle," + prefix + "Chord)");
		add(pointLabel + "=Intersect(" + prefix + "Normal," + prefix + "Bisector)");
		GeoLocusV2 locus = add(locusLabel + "=LocusV2(" + pointLabel + "," + points
				+ "Driver)");
		assertNotNull(locus);
		return locus;
	}

	/**
	 * A certified non-injective locus (PRE-G9B-CU2 step T11, declared conformity): the
	 * orthogonal projection of a point of the unit circle onto the x-axis (class v1), whose
	 * preimages {@code t} and {@code -t} share one position. It replaces expression-driven
	 * self-intersecting fixtures, which are outside class v3.
	 */
	GeoLocusV2 projectedCircle() {
		add("projectedCircle=Circle((0,0),1)");
		add("projectedDriver=Point(projectedCircle)");
		add("projectedAxis=Line((0,0),(1,0))");
		add("projectedNormal=PerpendicularLine(projectedDriver,projectedAxis)");
		add("Q=Intersect(projectedAxis,projectedNormal)");
		GeoLocusV2 locus = add("L=LocusV2(Q,projectedDriver)");
		assertNotNull(locus);
		return locus;
	}

	GeoLocusV2 createDisconnectedLine() {
		return createScalarLocus("L", "s", "Q", "(s,0)",
				"{false,{-2,-1,true,true},{1,2,true,true}}");
	}

	GeoLocusV2 createScalarLocus(String locusLabel, String coordinateLabel,
			String pointLabel, String pointExpression, String domainExpression) {
		add(coordinateLabel + "=0");
		add(pointLabel + "=" + pointExpression);
		add("D=" + domainExpression);
		GeoLocusV2 locus = add(locusLabel + "=LocusV2(" + pointLabel + ","
				+ coordinateLabel + ",D)");
		assertNotNull(locus);
		return locus;
	}

	GeoPoint semanticPoint(GeoLocusV2 locus, double parameter) {
		GeoPoint point = add("P=Point(" + locus.getLabelSimple() + ",\""
				+ BRANCH + "\"," + parameter + ")");
		assertNotNull(point);
		return point;
	}

	GeoLocusMetricResult totalMetric(GeoLocusV2 locus) {
		GeoLocusMetricResult metric = add("M=LocusLength("
				+ locus.getLabelSimple() + ")");
		assertNotNull(metric);
		return metric;
	}

	GeoLocusIntersectionResult intersect(GeoLocusV2 locus,
			String targetDefinition) {
		add("target:" + targetDefinition);
		GeoLocusIntersectionResult result = add("R=Intersect("
				+ locus.getLabelSimple() + ",target)");
		assertNotNull(result);
		return result;
	}

	List<GeoLocusIntersectionResult> allPublicIntersectionFamilies(
			GeoLocusV2 locus) {
		add("u0Line:y=1");
		add("u0Segment=Segment((-2,1),(2,1))");
		add("u0Ray=Ray((-2,1),(2,1))");
		add("u0Circle=Circle((0,0),1)");
		add("u0Ellipse=Ellipse((-1,0),(1,0),3)");
		add("u0Parabola=Parabola((0,1),y=-1)");
		add("u0Hyperbola=Hyperbola((-1,0),(1,0),0.5)");
		add("u0Function=Function(x^2,-2,2)");
		add("u0Implicit=ImplicitCurve(x^3+y^3-1)");
		String[] targets = {"u0Line", "u0Segment", "u0Ray", "u0Circle",
				"u0Ellipse", "u0Parabola", "u0Hyperbola", "u0Function",
				"u0Implicit"};
		java.util.ArrayList<GeoLocusIntersectionResult> results =
				new java.util.ArrayList<>();
		for (int index = 0; index < targets.length; index++) {
			GeoLocusIntersectionResult result = add("R" + index + "=Intersect("
					+ locus.getLabelSimple() + "," + targets[index] + ")");
			assertNotNull(result);
			results.add(result);
		}
		add("u=0");
		add("U=(u,u)");
		add("Du={false,{-2,2,true,true}}");
		add("L2=LocusV2(U,u,Du)");
		GeoLocusIntersectionResult pair = add("R9=Intersect("
				+ locus.getLabelSimple() + ",L2)");
		assertNotNull(pair);
		results.add(pair);
		return List.copyOf(results);
	}

	String firstToken(GeoLocusIntersectionResult result) {
		assertNotNull(result.getIntersectionResult());
		return result.getIntersectionResult().getFiniteSolutions().get(0)
				.getIdentity().getRootToken();
	}

	GeoPoint tokenPoint(GeoLocusIntersectionResult result, String token) {
		GeoPoint point = add("X=Intersect(" + result.getLabelSimple() + ",\""
				+ token + "\")");
		assertNotNull(point);
		return point;
	}

	GeoElement requireLookup(String label) {
		GeoElement result = lookup(label);
		assertNotNull(result);
		return result;
	}

	/**
	 * PRE-G9B-CU2 step T11: the total length of a locus whose existence is not established
	 * is refused as an incomplete aggregate; it is never an estimate of an unproven curve.
	 */
	static void assertNoTotalWithoutExistence(LocusMetricResult2D rich, String where) {
		String message = where + ": " + rich.getComputationStatus() + " "
				+ rich.getDiagnostics();
		assertEquals(MetricComputationStatus.INVALID_QUERY, rich.getComputationStatus(),
				message);
		assertEquals(MetricGuaranteeClass2D.NOT_ESTABLISHED, rich.getGuaranteeClass(),
				message);
		assertFalse(rich.isScalarAdmissible(), message);
		assertTrue(rich.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.INCOMPLETE_AGGREGATE), message);
	}

	GeoNumeric scalarLength(GeoLocusV2 locus) {
		GeoNumeric scalar = add("a=Length(" + locus.getLabelSimple() + ")");
		assertNotNull(scalar);
		return scalar;
	}
}

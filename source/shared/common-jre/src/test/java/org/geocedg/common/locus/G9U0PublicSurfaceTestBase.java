/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
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

	GeoLocusV2 createPeriodicCircle() {
		return createScalarLocus("L", "s", "Q", "(cos(s),sin(s))",
				"{true,{0,2*pi,true,false}}");
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

	GeoNumeric scalarLength(GeoLocusV2 locus) {
		GeoNumeric scalar = add("a=Length(" + locus.getLabelSimple() + ")");
		assertNotNull(scalar);
		return scalar;
	}
}

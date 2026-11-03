/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Native kernel compatibility, adapted from the independent audit's thirteen-family
 * inventory and its corrected implicit-equation follow-up. Original probes/results
 * remain in artifacts/cu7-independent-audit. This is not physical mouse evidence
 * or a claim that an auxiliary native parameter is a durable Locus V2 address.
 */
class R3NativeFamilyCompatibilityTest extends G9U0PublicSurfaceTestBase {

	static Stream<Arguments> nativeFamilies() {
		return Stream.of(
				Arguments.of("line", "Line((0,0),(1,1))", .5, .5, 1.2, 1.2),
				Arguments.of("ray", "Ray((0,0),(1,1))", .5, .5, 1.2, 1.2),
				Arguments.of("segment", "Segment((0,0),(2,1))", .5, .25, 1.2, .6),
				Arguments.of("circle", "Circle((0,0),1)", .8, .6, .6, .8),
				Arguments.of("ellipse", "Ellipse((-1,0),(1,0),2)", 1, 1.5, .5, 1.6),
				Arguments.of("parabola", "Parabola((0,1),y=-1)", 1, .25, 1.6, .64),
				Arguments.of("hyperbola", "Hyperbola((-2,0),(2,0),1)", 1.2, 1, 1.5, 1.9),
				Arguments.of("arc", "CircularArc((0,0),(1,0),(0,1))", .8, .6, .6, .8),
				Arguments.of("parametric", "Curve(t,t^2,t,-2,2)", .8, .64, 1.1, 1.21),
				Arguments.of("function", "Function(x^2,-2,2)", .8, .64, 1.1, 1.21),
				Arguments.of("implicit", "x^4+y^4=1", .8, .85, .9, .7),
				Arguments.of("spline", "Spline({(-2,0),(-1,1),(1,1),(2,0)})", -.5, 1.2, .5, 1.2),
				Arguments.of("polyline", "Polyline((0,0),(1,1),(2,0))", .5, .5, 1.5, .5));
	}

	@ParameterizedTest(name = "native {0}")
	@MethodSource("nativeFamilies")
	void nativeMovementRetainsPathAndPropagatesDependents(String family, String expression,
			double x0, double y0, double x1, double y1) {
		GeoElement source = add("nativePath" + ("implicit".equals(family) ? ": " : "=")
				+ expression);
		Path path = assertInstanceOf(Path.class, source);
		GeoPoint point = add("NativeP=Point(nativePath)");
		AlgoPointOnPath parent = assertInstanceOf(AlgoPointOnPath.class,
				point.getParentAlgorithm());
		assertTrue(parent.isChangeable(point));
		GeoPoint dependent = add("NativeDependent=NativeP+(1,2)");
		point.setCoords(x0, y0, 1);
		point.updateCascade();
		double oldX = point.getInhomX();
		double oldY = point.getInhomY();
		point.setCoords(x1, y1, 1);
		point.updateCascade();
		assertTrue(point.isDefined(), family);
		assertSame(path, point.getPath());
		assertSame(parent, point.getParentAlgorithm());
		assertTrue(Double.isFinite(point.getPathParameter().t));
		assertTrue(Math.hypot(point.getInhomX() - oldX, point.getInhomY() - oldY) > 1e-6);
		GeoPoint witness = new GeoPoint(getConstruction());
		witness.setCoords(point.getInhomX(), point.getInhomY(), 1);
		assertNull(witness.getPath());
		assertTrue(path.isOnPath(witness, 1e-5), family + ": independent membership witness");
		if ("implicit".equals(family)) {
			assertEquals(1, Math.pow(point.getInhomX(), 4) + Math.pow(point.getInhomY(), 4),
					1e-5);
		}
		assertEquals(point.getInhomX() + 1, dependent.getInhomX(), 1e-10);
		assertEquals(point.getInhomY() + 2, dependent.getInhomY(), 1e-10);
		getKernel().updateConstruction();
		assertTrue(point.isDefined());
		assertSame(path, point.getPath());
		assertSame(parent, point.getParentAlgorithm());
		assertEquals(point.getInhomX() + 1, dependent.getInhomX(), 1e-10);
		assertEquals(point.getInhomY() + 2, dependent.getInhomY(), 1e-10);
	}
}

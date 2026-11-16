/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Actual inherited intersections check the proof; samples never establish eligibility. */
class NativeRadialRayGuardTest extends BaseUnitTest {

	private static Operand point(double x, double y, double z) {
		return Operand.point(x, y, z, x / z, y / z);
	}

	private static List<Double> angles() {
		List<Double> angles = new ArrayList<>(List.of(0.0, -0.0, Math.PI / 2,
				-Math.PI / 2, Math.PI, -Math.PI, Math.nextDown(Math.PI / 2),
				Math.nextUp(Math.PI / 2), Double.MAX_VALUE, -Double.MAX_VALUE,
				1E100, -1E100));
		for (int index = -64; index <= 64; index++) {
			angles.add(index * Math.PI / 32);
		}
		return angles;
	}

	private void actualRoots(double x, double y, double z, double cx, double cy,
			double lowerRadius, double upperRadius) {
		Operand source = point(x, y, z), center = point(cx, cy, 1);
		var rotation = NativeVariableRotationGuard2D.enclose(x, y, z, cx, cy);
		assertNotNull(rotation);
		double bound = NativeRadialRayGuard2D.enclose(source, center, rotation,
				new SplineOutwardInterval2D(lowerRadius, upperRadius));
		assertTrue(Double.isFinite(bound) && bound > 0,
				"A full phase cover is required before any native sample is checked");
		getKernel().setContinuous(false);
		assertInstanceOf(GeoPoint.class, add("CenterGuard=(" + cx + "," + cy + ")"));
		GeoPoint nativeSource = assertInstanceOf(GeoPoint.class,
				add("SourceGuard=(" + x / z + "," + y / z + ")"));
		nativeSource.setCoords(x, y, z);
		nativeSource.updateCascade();
		GeoNumeric angle = add("phaseGuard=0");
		GeoNumeric radius = add("radiusGuard=" + lowerRadius);
		assertInstanceOf(GeoPoint.class, add("RotatedGuard=Rotate(SourceGuard,phaseGuard,CenterGuard)"));
		add("rayGuard=Ray(CenterGuard,RotatedGuard)");
		GeoConic circle = assertInstanceOf(GeoConic.class,
				add("circleGuard=Circle(CenterGuard,radiusGuard)"));
		GeoPoint root = assertInstanceOf(GeoPoint.class,
				add("RootGuard=Intersect(circleGuard,rayGuard,1)"));
		for (double r : new double[] {lowerRadius, (lowerRadius + upperRadius) / 2, upperRadius}) {
			radius.setValue(r); radius.updateCascade();
			double nativeMatrix = -r * r;
			nativeMatrix += cx * cx;
			nativeMatrix += cy * cy;
			assertEquals(Double.doubleToLongBits(nativeMatrix),
					Double.doubleToLongBits(circle.getMatrix()[2]), "Native matrix operation order");
			for (double phase : angles()) {
				angle.setValue(phase); angle.updateCascade();
				assertTrue(root.isFinite(), "Native positive root at r=" + r + ", phase=" + phase);
				assertTrue(Math.abs(root.inhomX) <= bound && Math.abs(root.inhomY) <= bound,
						"The actual inherited root must lie in the computed magnitude bound");
				GeoPoint rotated = (GeoPoint) lookup("RotatedGuard");
				double vx = rotated.inhomX - cx, vy = rotated.inhomY - cy;
				assertTrue((root.inhomX - cx) * vx + (root.inhomY - cy) * vy > 0,
						"The computed root is on the positive ray, not its opposite");
				assertEquals(r, Math.hypot(root.inhomX - cx, root.inhomY - cy), 1E-5);
			}
		}
	}

	@Test void fullPhaseProofContainsNativePositiveRootsAtAxesAndHugeAngles() {
		actualRoots(1, 0, 1, 0, 0, 2, 3);
	}

	@Test void translatedCenterAndHomogeneousSourceKeepTheirNativeRoots() {
		actualRoots(6, 8, 2, 1, -2, .5, 2);
	}

	@Test void radiusMuchLargerThanCenterUsesTheActualSequentialMatrix() {
		double radius = 1E8;
		double nativeMatrix = -radius * radius;
		nativeMatrix += 1;
		nativeMatrix += 1;
		assertNotEquals(Double.doubleToLongBits(2 - radius * radius),
				Double.doubleToLongBits(nativeMatrix),
				"The fixture must distinguish regrouping the center squares from native order");
		actualRoots(2, 1, 1, 1, 1, radius, radius + 2);
	}

	@Test void positiveRadiusInsideNativeRayToleranceCannotProveUniquePositiveSelection() {
		Operand source = point(1, 0, 1), center = point(0, 0, 1);
		var rotation = NativeVariableRotationGuard2D.enclose(1, 0, 1, 0, 0);
		assertNotNull(rotation);
		// The radius exceeds degeneration precision, but the opposite root can
		// still satisfy the inherited ray's 1e-5 containment tolerance.
		assertTrue(Double.isNaN(NativeRadialRayGuard2D.enclose(source, center, rotation,
				SplineOutwardInterval2D.point(1E-6))));
	}

	@Test void absentRotationAndNonpositiveRadiusCannotProduceAnEligibilityBound() {
		Operand source = point(1, 0, 1), center = point(0, 0, 1);
		var rotation = NativeVariableRotationGuard2D.enclose(1, 0, 1, 0, 0);
		assertTrue(Double.isNaN(NativeRadialRayGuard2D.enclose(source, center, null,
				SplineOutwardInterval2D.point(1))));
		assertTrue(Double.isNaN(NativeRadialRayGuard2D.enclose(source, center, rotation,
				new SplineOutwardInterval2D(0, 1))));
		assertTrue(Double.isNaN(NativeRadialRayGuard2D.enclose(source, center, rotation,
				new SplineOutwardInterval2D(-2, -1))));
	}
}

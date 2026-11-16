/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.util.MyMath;
import org.junit.jupiter.api.Test;

/** Native-value/error oracles; deterministic samples check, rather than create, the proof. */
class GeoCeDG120NativeVariableRotationGuardTest {

	private static double[] nativeRotation(double x, double y, double z,
			double cx, double cy, double angle) {
		double cosine = MyMath.cos(angle);
		double sine = Math.sin(angle);
		double qx = z * cx;
		double qy = z * cy;
		double rawX = (x - qx) * cosine + (qy - y) * sine + qx;
		double rawY = (x - qx) * sine + (y - qy) * cosine + qy;
		// GeoPoint.updateCoords flips a negative homogeneous sign exactly before division.
		double outputX = z == 1 ? rawX : rawX / z;
		double outputY = z == 1 ? rawY : rawY / z;
		return new double[] {outputX, outputY, outputX - cx, outputY - cy};
	}

	private static BigDecimal exact(double value) { return new BigDecimal(value); }

	/** Exact rational formula with represented trig coefficients; test z is a power of two. */
	private static BigDecimal[] reference(double x, double y, double z,
			double cx, double cy, double cosine, double sine) {
		BigDecimal qx = exact(z).multiply(exact(cx));
		BigDecimal qy = exact(z).multiply(exact(cy));
		BigDecimal u = exact(x).subtract(qx);
		BigDecimal v = exact(y).subtract(qy);
		return new BigDecimal[] {
				u.multiply(exact(cosine)).subtract(v.multiply(exact(sine))).add(qx).divide(exact(z)),
				u.multiply(exact(sine)).add(v.multiply(exact(cosine))).add(qy).divide(exact(z))};
	}

	private static List<Double> angles() {
		List<Double> result = new ArrayList<>(List.of(0.0, -0.0, Double.MIN_VALUE,
				-Double.MIN_VALUE, Math.PI / 2, -Math.PI / 2, Math.PI, -Math.PI,
				Math.nextDown(Math.PI / 2), Math.nextUp(Math.PI / 2),
				Double.MAX_VALUE, -Double.MAX_VALUE, 1E100, -1E100));
		for (int index = -64; index <= 64; index++) { result.add(index * Math.PI / 32); }
		return result;
	}

	private static void check(double x, double y, double z, double cx, double cy) {
		var proof = NativeVariableRotationGuard2D.enclose(x, y, z, cx, cy);
		assertNotNull(proof);
		assertTrue(proof.getDirectionLower() > Math.sqrt(2) * Kernel.STANDARD_PRECISION);
		for (double angle : angles()) {
			double[] nativeValue = nativeRotation(x, y, z, cx, cy, angle);
			assertTrue(proof.getNativeXLower() <= nativeValue[0]
					&& nativeValue[0] <= proof.getNativeXUpper(), "native x at " + angle);
			assertTrue(proof.getNativeYLower() <= nativeValue[1]
					&& nativeValue[1] <= proof.getNativeYUpper(), "native y at " + angle);
			BigDecimal squaredNorm = exact(nativeValue[2]).pow(2).add(exact(nativeValue[3]).pow(2));
			assertTrue(exact(proof.getDirectionLower()).pow(2).compareTo(squaredNorm) <= 0);
			assertTrue(exact(proof.getDirectionUpper()).pow(2).compareTo(squaredNorm) >= 0);
			BigDecimal[] nativeReference = reference(x, y, z, cx, cy, MyMath.cos(angle), Math.sin(angle));
			assertTrue(exact(nativeValue[0]).subtract(nativeReference[0]).abs()
					.compareTo(exact(proof.getRoundoffX())) <= 0, "roundoff x at " + angle);
			assertTrue(exact(nativeValue[1]).subtract(nativeReference[1]).abs()
					.compareTo(exact(proof.getRoundoffY())) <= 0, "roundoff y at " + angle);
			BigDecimal[] unsnappedReference = reference(x, y, z, cx, cy, Math.cos(angle), Math.sin(angle));
			assertTrue(exact(nativeValue[0]).subtract(unsnappedReference[0]).abs()
					.compareTo(exact(proof.getErrorX())) <= 0, "trig/roundoff x at " + angle);
			assertTrue(exact(nativeValue[1]).subtract(unsnappedReference[1]).abs()
					.compareTo(exact(proof.getErrorY())) <= 0, "trig/roundoff y at " + angle);
		}
	}

	@Test
	void allFiniteAngleGuardContainsNativeValuesIncludingCosineSnapAndHugeAngles() {
		check(3, 1, 1, 1, 1);
		check(-4, 3, 1, -2, 5);
		assertEquals(0, MyMath.cos(Math.PI / 2), 0);
		assertTrue(Math.cos(Math.PI / 2) != 0, "The fixture must exercise the native snap");
	}

	@Test
	void homogeneousNormalizationAndNegativeSignsRetainTheirOwnArithmeticError() {
		check(6, 2, 2, 1, 1);
		check(-6, -2, -2, 1, 1);
		check(.5, -.25, .5, -.5, 1);
		check(0x1.0p-23, 0x1.0p-24, 0x1.0p-24, .5, -.5);
	}

	@Test
	void subnormalCoordinatesAndLargeFiniteCoordinatesUseOutwardAbsoluteErrors() {
		check(1, Double.MIN_VALUE, 1, -0.0, 0.0);
		check(1E100, -1E100, 1, 1E99, -1E99);
	}

	@Test
	void nonfiniteOverflowAbsorptionAndInsufficientDirectionFailClosed() {
		for (double[] data : new double[][] {
				{1, 0, 2, 1E308, 0}, {.5, 0, 1, 1E308, 0},
				{1E16 + 2, 0, 1, 1E16, 0}, {0, 0, 1, 0, 0},
				{1E-9, 0, 1, 0, 0}, {Double.MIN_VALUE, 0, 1, 0, 0},
				{1, 0, 0, 0, 0}, {1, 0, Kernel.STANDARD_PRECISION, 0, 0},
				{1, 0, Double.MIN_VALUE, 0, 0}, {Double.NaN, 0, 1, 0, 0},
				{1, Double.POSITIVE_INFINITY, 1, 0, 0}, {1, 0, 1, Double.NaN, 0}}) {
			assertNull(NativeVariableRotationGuard2D.enclose(
					data[0], data[1], data[2], data[3], data[4]));
		}
	}

	@Test
	void pointwiseRoundoffErrorDoesNotAssertAnUnknownAngularApproximationIsAccurate() {
		var proof = NativeVariableRotationGuard2D.enclose(1, 0, 1, 0, 0);
		assertNotNull(proof);
		assertTrue(proof.getNativeXLower() <= -1 && 1 <= proof.getNativeXUpper());
		assertTrue(proof.getErrorX() < 1E-12);
		double angularDifference = Math.abs(nativeRotation(1, 0, 1, 0, 0, 0)[0] - (-1));
		assertEquals(2, angularDifference, 0);
		assertTrue(angularDifference > proof.getErrorX(),
				"The same-angle error cannot be reused for a different estimated angle");
	}
}

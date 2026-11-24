/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Exact capture and the library's own round-half-even (no BigDecimal rounding): hand cases
 * for ties, powers of two, binade boundaries, subnormals and overflow, plus the IEEE
 * division of two doubles as an independent correctly rounded oracle.
 */
final class BSplineExactRationalTest {

	private static final BigInteger TWO = BigInteger.valueOf(2);

	private static long bits(double value) {
		return Double.doubleToRawLongBits(value);
	}

	private static BSplineExactRational exact(double value) {
		return BSplineExactRational.ofDouble(value);
	}

	/** Exact midpoint of two doubles. */
	private static BSplineExactRational midpoint(double a, double b) {
		return exact(a).add(exact(b)).divide(BSplineExactRational.ofLong(2));
	}

	private static BSplineExactRational tiny(int exponent) {
		return BSplineExactRational.of(BigInteger.ONE, TWO.pow(exponent));
	}

	@Test
	void captureIsExact() {
		Random random = new Random(BSplineTestData.SEED + 31);
		for (int i = 0; i < 2000; i++) {
			double value = Double.longBitsToDouble(random.nextLong());
			if (!Double.isFinite(value)) {
				continue;
			}
			BSplineExactRational r = exact(value);
			BigDecimal expected = new BigDecimal(value);
			BigDecimal actual = new BigDecimal(r.numerator()).divide(new BigDecimal(
					r.denominator()));
			assertEquals(0, expected.compareTo(actual));
			assertEquals(bits(value == 0.0 ? 0.0 : value), bits(r.roundHalfEven()));
		}
	}

	@Test
	void powersOfTwoAndExtremesRoundTrip() {
		for (int k = -1074; k <= 1023; k++) {
			double value = Math.scalb(1.0, k);
			assertEquals(bits(value), bits(exact(value).roundHalfEven()));
			assertEquals(bits(-value), bits(exact(-value).roundHalfEven()));
		}
		for (double value : new double[] {Double.MIN_VALUE, Double.MIN_NORMAL,
				Math.nextDown(Double.MIN_NORMAL), Double.MAX_VALUE, 1.0, Math.nextUp(1.0),
				Math.nextDown(2.0), 0.1, 1.0 / 3.0}) {
			assertEquals(bits(value), bits(exact(value).roundHalfEven()));
		}
		assertEquals(bits(0.0), bits(exact(-0.0).roundHalfEven()));
	}

	@Test
	void tiesGoToEven() {
		double one = 1.0;
		double up1 = Math.nextUp(one);
		double up2 = Math.nextUp(up1);
		assertEquals(bits(one), bits(midpoint(one, up1).roundHalfEven()));
		assertEquals(bits(up2), bits(midpoint(up1, up2).roundHalfEven()));
		// Binade boundary: nextDown(2) has an odd significand, so the tie goes up to 2.
		assertEquals(bits(2.0), bits(midpoint(Math.nextDown(2.0), 2.0).roundHalfEven()));
		assertEquals(bits(-2.0), bits(midpoint(-Math.nextDown(2.0), -2.0).roundHalfEven()));
		// Subnormal ties.
		assertEquals(bits(0.0), bits(midpoint(0.0, Double.MIN_VALUE).roundHalfEven()));
		assertEquals(bits(2 * Double.MIN_VALUE), bits(midpoint(Double.MIN_VALUE,
				2 * Double.MIN_VALUE).roundHalfEven()));
		assertEquals(bits(Double.MIN_NORMAL), bits(midpoint(Math.nextDown(Double.MIN_NORMAL),
				Double.MIN_NORMAL).roundHalfEven()));
	}

	@Test
	void nearTiesGoToTheNearestNeighbour() {
		double up1 = Math.nextUp(1.0);
		BSplineExactRational mid = midpoint(1.0, up1);
		assertEquals(bits(up1), bits(mid.add(tiny(200)).roundHalfEven()));
		assertEquals(bits(1.0), bits(mid.subtract(tiny(200)).roundHalfEven()));
		BSplineExactRational below2 = midpoint(Math.nextDown(2.0), 2.0);
		assertEquals(bits(Math.nextDown(2.0)), bits(below2.subtract(tiny(300))
				.roundHalfEven()));
		assertEquals(bits(2.0), bits(below2.add(tiny(300)).roundHalfEven()));
		BSplineExactRational third = BSplineExactRational.ofLong(1)
				.divide(BSplineExactRational.ofLong(3));
		assertEquals(bits(1.0 / 3.0), bits(third.roundHalfEven()));
		assertEquals(bits(-0.1), bits(BSplineExactRational.ofLong(-1)
				.divide(BSplineExactRational.ofLong(10)).roundHalfEven()));
	}

	@Test
	void overflowAndUnderflowFollowIeee() {
		double max = Double.MAX_VALUE;
		BSplineExactRational halfUlpAboveMax = exact(max).add(exact(Math.ulp(max) / 2));
		// MAX_VALUE has an odd significand: the tie rounds to 2^1024, which overflows.
		assertEquals(bits(Double.POSITIVE_INFINITY), bits(halfUlpAboveMax.roundHalfEven()));
		assertEquals(bits(Double.NEGATIVE_INFINITY),
				bits(halfUlpAboveMax.negate().roundHalfEven()));
		assertEquals(bits(max), bits(halfUlpAboveMax.subtract(tiny(10)).roundHalfEven()));
		assertEquals(bits(Double.POSITIVE_INFINITY), bits(BSplineExactRational.of(
				BigInteger.ONE.shiftLeft(1100), BigInteger.ONE).roundHalfEven()));
		assertEquals(bits(0.0), bits(tiny(1076).roundHalfEven()));
		assertEquals(bits(Double.MIN_VALUE), bits(tiny(1074).add(tiny(1076))
				.roundHalfEven()));
	}

	@Test
	void quotientsOfDoublesMatchIeeeDivision() {
		Random random = new Random(BSplineTestData.SEED + 32);
		for (int i = 0; i < 4000; i++) {
			double a = Math.scalb(random.nextDouble() - 0.5, random.nextInt(2100) - 1050);
			double b = Math.scalb(random.nextDouble() - 0.5, random.nextInt(2100) - 1050);
			if (a == 0.0 || b == 0.0 || !Double.isFinite(a) || !Double.isFinite(b)) {
				continue;
			}
			double expected = a / b;
			double actual = exact(a).divide(exact(b)).roundHalfEven();
			if (expected == 0.0) {
				// IEEE keeps the sign of a zero quotient; the exact value has none.
				assertEquals(0.0, Math.abs(actual));
			} else {
				assertEquals(bits(expected), bits(actual), a + " / " + b);
			}
		}
	}
}

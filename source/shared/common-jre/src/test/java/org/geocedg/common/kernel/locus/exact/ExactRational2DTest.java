/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** Exact binary64 capture, arithmetic and outward rounding of {@link ExactRational2D}. */
final class ExactRational2DTest {
	private static final long SEED = 20261021L;

	@Test
	void decimalTenthIsCapturedExactly() {
		ExactRational2D tenth = ExactRational2D.ofBinary64(0.1);

		assertEquals(BigInteger.valueOf(3602879701896397L), tenth.numerator());
		assertEquals(BigInteger.ONE.shiftLeft(55), tenth.denominator());
		assertEquals(0, new BigDecimal(0.1).compareTo(toBigDecimal(tenth)));
		assertNotEquals(ExactRational2D.of(1, 10), tenth);
		assertTrue(tenth.compareTo(ExactRational2D.of(1, 10)) > 0);
	}

	@Test
	void bothSignedZerosAreZero() {
		ExactRational2D negativeZero = ExactRational2D.ofBinary64(-0.0);

		assertSame(ExactRational2D.ZERO, negativeZero);
		assertEquals(ExactRational2D.ofBinary64(0.0), negativeZero);
		assertTrue(negativeZero.isZero());
		assertEquals(0, negativeZero.signum());
		assertEquals("0/1", negativeZero.toString());
	}

	@Test
	void subnormalsAndExtremesAreCapturedExactly() {
		assertEquals(ExactRational2D.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(1074)),
				ExactRational2D.ofBinary64(Double.MIN_VALUE));
		assertEquals(ExactRational2D.of(BigInteger.valueOf(3), BigInteger.ONE.shiftLeft(1074)),
				ExactRational2D.ofBinary64(3 * Double.MIN_VALUE));
		assertEquals(ExactRational2D.of(BigInteger.ONE.shiftLeft(52).subtract(BigInteger.ONE)
				.negate(), BigInteger.ONE.shiftLeft(1074)),
				ExactRational2D.ofBinary64(-Math.nextDown(Double.MIN_NORMAL)));
		assertEquals(ExactRational2D.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(1022)),
				ExactRational2D.ofBinary64(Double.MIN_NORMAL));
		BigInteger largest = BigInteger.ONE.shiftLeft(53).subtract(BigInteger.ONE)
				.shiftLeft(971);
		assertEquals(ExactRational2D.of(largest, BigInteger.ONE),
				ExactRational2D.ofBinary64(Double.MAX_VALUE));
		assertEquals(ExactRational2D.of(largest.negate(), BigInteger.ONE),
				ExactRational2D.ofBinary64(-Double.MAX_VALUE));
	}

	@Test
	void everyPowerOfTwoIsCapturedExactly() {
		for (int exponent = -1074; exponent <= 1023; exponent++) {
			ExactRational2D power = ExactRational2D.ofBinary64(Math.scalb(1.0, exponent));
			BigInteger magnitude = BigInteger.ONE.shiftLeft(Math.abs(exponent));
			ExactRational2D expected = exponent >= 0
					? ExactRational2D.of(magnitude, BigInteger.ONE)
					: ExactRational2D.of(BigInteger.ONE, magnitude);
			assertEquals(expected, power, "2^" + exponent);
		}
	}

	@Test
	void randomBinary64ValuesAreCapturedExactlyAndReduced() {
		Random random = new Random(SEED);
		int checked = 0;
		while (checked < 2000) {
			double value = Double.longBitsToDouble(random.nextLong());
			if (Double.isNaN(value) || Double.isInfinite(value)) {
				continue;
			}
			ExactRational2D exact = ExactRational2D.ofBinary64(value);
			assertEquals(0, new BigDecimal(value).compareTo(toBigDecimal(exact)));
			assertEquals(1, exact.denominator().signum());
			assertEquals(BigInteger.ONE, exact.numerator().gcd(exact.denominator()));
			checked++;
		}
	}

	@Test
	void nonFiniteValuesAreRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> ExactRational2D.ofBinary64(Double.NaN));
		assertThrows(IllegalArgumentException.class,
				() -> ExactRational2D.ofBinary64(Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class,
				() -> ExactRational2D.ofBinary64(Double.NEGATIVE_INFINITY));
	}

	@Test
	void constructionReducesAndNormalizesTheSign() {
		ExactRational2D value = ExactRational2D.of(6, -4);

		assertEquals(BigInteger.valueOf(-3), value.numerator());
		assertEquals(BigInteger.valueOf(2), value.denominator());
		assertEquals("-3/2", value.toString());
		assertEquals("5/1", ExactRational2D.of(5).toString());
		assertSame(ExactRational2D.ZERO, ExactRational2D.of(0, -7));
		assertThrows(ArithmeticException.class, () -> ExactRational2D.of(1, 0));
		assertThrows(ArithmeticException.class,
				() -> ExactRational2D.of(BigInteger.ONE, BigInteger.ZERO));
	}

	@Test
	void arithmeticIdentitiesHoldExactly() {
		Random random = new Random(SEED + 1);
		for (int i = 0; i < 500; i++) {
			ExactRational2D a = randomRational(random);
			ExactRational2D b = randomRational(random);
			ExactRational2D c = randomRational(random);

			assertEquals(a, a.add(b).subtract(b));
			assertEquals(a.add(b), b.add(a));
			assertEquals(a.multiply(b), b.multiply(a));
			assertEquals(a.multiply(b.add(c)), a.multiply(b).add(a.multiply(c)));
			assertTrue(a.add(a.negate()).isZero());
			assertEquals(a, a.multiply(ExactRational2D.ONE));
			assertEquals(Integer.signum(a.compareTo(b)), a.subtract(b).signum());
			assertEquals(a.abs(), a.signum() < 0 ? a.negate() : a);
			if (!b.isZero()) {
				assertEquals(a, a.multiply(b).divide(b));
				assertEquals(a, a.divide(b).multiply(b));
			}
			ExactRational2D middle = ExactRational2D.midpoint(a, b);
			assertEquals(a.add(b), middle.add(middle));
		}
		assertThrows(ArithmeticException.class,
				() -> ExactRational2D.ONE.divide(ExactRational2D.ZERO));
	}

	@Test
	void comparisonEqualityAndBitLength() {
		ExactRational2D third = ExactRational2D.of(1, 3);

		assertEquals(third, ExactRational2D.of(-2, -6));
		assertEquals(third.hashCode(), ExactRational2D.of(-2, -6).hashCode());
		assertTrue(third.compareTo(ExactRational2D.of(1, 2)) < 0);
		assertTrue(ExactRational2D.of(-1, 2).compareTo(third) < 0);
		assertEquals(0, third.compareTo(ExactRational2D.of(2, 6)));
		assertEquals(ExactRational2D.of(5, 12),
				ExactRational2D.midpoint(third, ExactRational2D.of(1, 2)));
		assertEquals(9, ExactRational2D.of(-255, 256).bitLength());
		assertEquals(4, ExactRational2D.of(-8, 3).bitLength());
		assertEquals(1, ExactRational2D.ONE.bitLength());
		assertFalse(third.isZero());
	}

	@Test
	void outwardEnclosureIsExactForRepresentableValues() {
		Random random = new Random(SEED + 2);
		for (int i = 0; i < 2000; i++) {
			double value = Double.longBitsToDouble(random.nextLong());
			if (Double.isNaN(value) || Double.isInfinite(value) || value == 0.0) {
				continue;
			}
			double[] enclosure = ExactRational2D.ofBinary64(value).outwardDoubleEnclosure();
			assertEquals(Double.doubleToRawLongBits(value),
					Double.doubleToRawLongBits(enclosure[0]));
			assertEquals(Double.doubleToRawLongBits(value),
					Double.doubleToRawLongBits(enclosure[1]));
		}
		double[] zero = ExactRational2D.ZERO.outwardDoubleEnclosure();
		assertEquals(0.0, zero[0]);
		assertEquals(0.0, zero[1]);
	}

	@Test
	void outwardEnclosureBracketsOtherValuesBetweenAdjacentDoubles() {
		assertAdjacentBracket(ExactRational2D.of(1, 3));
		assertAdjacentBracket(ExactRational2D.of(-1, 3));
		assertAdjacentBracket(ExactRational2D.of(2, 3).add(ExactRational2D.of(BigInteger.ONE,
				BigInteger.ONE.shiftLeft(1060))));
		Random random = new Random(SEED + 3);
		for (int i = 0; i < 2000; i++) {
			ExactRational2D value = randomRational(random);
			double[] enclosure = value.outwardDoubleEnclosure();
			assertBracket(value, enclosure);
			if (enclosure[0] != enclosure[1]) {
				assertEquals(Math.nextUp(enclosure[0]), enclosure[1]);
			}
		}
	}

	@Test
	void outwardEnclosureHandlesTheEndsOfTheBinary64Range() {
		ExactRational2D huge = ExactRational2D.of(BigInteger.ONE.shiftLeft(1100), BigInteger.ONE);
		ExactRational2D belowOverflow = ExactRational2D.of(
				BigInteger.ONE.shiftLeft(1024).subtract(BigInteger.ONE), BigInteger.ONE);
		ExactRational2D tiny = ExactRational2D.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(1100));
		ExactRational2D betweenSubnormals = ExactRational2D.of(BigInteger.valueOf(3),
				BigInteger.ONE.shiftLeft(1075));

		assertEnclosure(huge, Double.MAX_VALUE, Double.POSITIVE_INFINITY);
		assertEnclosure(huge.negate(), Double.NEGATIVE_INFINITY, -Double.MAX_VALUE);
		assertEnclosure(belowOverflow, Double.MAX_VALUE, Double.POSITIVE_INFINITY);
		assertEnclosure(tiny, 0.0, Double.MIN_VALUE);
		assertEnclosure(tiny.negate(), -Double.MIN_VALUE, -0.0);
		assertEnclosure(betweenSubnormals, Double.MIN_VALUE, 2 * Double.MIN_VALUE);
		assertBracket(betweenSubnormals, betweenSubnormals.outwardDoubleEnclosure());
	}

	private static void assertEnclosure(ExactRational2D value, double lower, double upper) {
		double[] enclosure = value.outwardDoubleEnclosure();
		assertEquals(lower, enclosure[0], value.toString());
		assertEquals(upper, enclosure[1], value.toString());
	}

	private static void assertAdjacentBracket(ExactRational2D value) {
		double[] enclosure = value.outwardDoubleEnclosure();
		assertBracket(value, enclosure);
		assertTrue(enclosure[0] < enclosure[1]);
		assertEquals(Math.nextUp(enclosure[0]), enclosure[1]);
	}

	private static void assertBracket(ExactRational2D value, double[] enclosure) {
		assertTrue(ExactRational2D.ofBinary64(enclosure[0]).compareTo(value) <= 0,
				value.toString());
		assertTrue(ExactRational2D.ofBinary64(enclosure[1]).compareTo(value) >= 0,
				value.toString());
	}

	private static ExactRational2D randomRational(Random random) {
		BigInteger numerator = new BigInteger(1 + random.nextInt(200), random);
		BigInteger denominator = new BigInteger(1 + random.nextInt(200), random)
				.add(BigInteger.ONE);
		if (random.nextBoolean()) {
			numerator = numerator.negate();
		}
		return ExactRational2D.of(numerator, denominator);
	}

	private static BigDecimal toBigDecimal(ExactRational2D value) {
		return new BigDecimal(value.numerator()).divide(new BigDecimal(value.denominator()));
	}
}

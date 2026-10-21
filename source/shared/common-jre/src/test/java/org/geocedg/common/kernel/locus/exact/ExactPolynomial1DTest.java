/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** Ring operations, division, gcd and exact evaluation of {@link ExactPolynomial1D}. */
final class ExactPolynomial1DTest {
	private static final long SEED = 20261021L;

	@Test
	void coefficientsAreNormalizedWithoutLeadingZeros() {
		ExactPolynomial1D linear = poly(1, 2, 0, 0);

		assertEquals(1, linear.degree());
		assertEquals(r(2), linear.leadingCoefficient());
		assertEquals(ExactRational2D.ZERO, linear.coefficient(7));
		assertSame(ExactPolynomial1D.ZERO, poly(0, 0));
		assertSame(ExactPolynomial1D.ZERO, ExactPolynomial1D.of());
		assertEquals(-1, ExactPolynomial1D.ZERO.degree());
		assertTrue(ExactPolynomial1D.ZERO.isZero());
		assertEquals(ExactRational2D.ZERO, ExactPolynomial1D.ZERO.leadingCoefficient());
		assertEquals(0, ExactPolynomial1D.ONE.degree());
		assertEquals(poly(0, 1), ExactPolynomial1D.X);
		assertThrows(IllegalArgumentException.class, () -> linear.coefficient(-1));
		assertThrows(NullPointerException.class,
				() -> ExactPolynomial1D.of(ExactRational2D.ONE, null));
	}

	@Test
	void ringOperationsAreExact() {
		ExactPolynomial1D xPlusOne = poly(1, 1);

		assertEquals(poly(1, 2, 1), xPlusOne.multiply(xPlusOne));
		assertEquals(poly(1, 3, 3, 1), xPlusOne.pow(3));
		assertEquals(ExactPolynomial1D.ONE, xPlusOne.pow(0));
		assertEquals(poly(0, 0, 0, 1), ExactPolynomial1D.X.pow(3));
		assertEquals(poly(2, 1, 1), xPlusOne.add(poly(1, 0, 1)));
		assertEquals(ExactPolynomial1D.ZERO, xPlusOne.subtract(poly(1, 1)));
		assertEquals(poly(-1, -1), xPlusOne.negate());
		assertEquals(ExactPolynomial1D.of(r(1, 2), r(1, 2)), xPlusOne.scale(r(1, 2)));
		assertEquals(ExactPolynomial1D.ZERO, xPlusOne.scale(ExactRational2D.ZERO));
		assertEquals(ExactPolynomial1D.ZERO, xPlusOne.multiply(ExactPolynomial1D.ZERO));
		assertEquals(poly(1), poly(1, 1).subtract(ExactPolynomial1D.X));
		assertThrows(IllegalArgumentException.class, () -> xPlusOne.pow(-1));
	}

	@Test
	void derivativeIsExact() {
		assertEquals(poly(-1, 0, 9), poly(5, -1, 0, 3).derivative());
		assertEquals(ExactPolynomial1D.of(r(1, 2), r(3, 2)),
				ExactPolynomial1D.of(r(7), r(1, 2), r(3, 4)).derivative());
		assertEquals(ExactPolynomial1D.ZERO, poly(4).derivative());
		assertEquals(ExactPolynomial1D.ZERO, ExactPolynomial1D.ZERO.derivative());
	}

	@Test
	void divisionSatisfiesTheEuclideanIdentity() {
		assertDivision(poly(-4, 0, 0, 1), poly(-2, 1));
		assertDivision(ExactPolynomial1D.of(r(1, 3), r(-2, 5), r(7, 2), r(1)),
				ExactPolynomial1D.of(r(3, 7), r(2, 9)));
		assertDivision(poly(1, 2), poly(1, 0, 1));
		assertDivision(poly(5, 3, 1), poly(3));
		Random random = new Random(SEED);
		for (int i = 0; i < 200; i++) {
			ExactPolynomial1D a = randomPolynomial(random, random.nextInt(8));
			ExactPolynomial1D b = randomPolynomial(random, random.nextInt(5));
			if (!b.isZero()) {
				assertDivision(a, b);
			}
		}
		ExactPolynomial1D[] smaller = poly(1, 2).divideAndRemainder(poly(1, 0, 1));
		assertEquals(ExactPolynomial1D.ZERO, smaller[0]);
		assertEquals(poly(1, 2), smaller[1]);
		assertThrows(ArithmeticException.class,
				() -> poly(1, 1).divideAndRemainder(ExactPolynomial1D.ZERO));
	}

	@Test
	void gcdIsMonicAndDividesBothArguments() {
		ExactPolynomial1D xMinusOne = poly(-1, 1);
		ExactPolynomial1D first = xMinusOne.multiply(poly(2, 1));
		ExactPolynomial1D second = xMinusOne.multiply(poly(-3, 1)).scale(r(-5, 3));

		assertEquals(xMinusOne, first.gcd(second));
		assertEquals(ExactPolynomial1D.ONE, poly(1, 0, 1).gcd(poly(-1, 1)));
		assertEquals(poly(-1, 1).multiply(poly(1, 0, 1)),
				poly(-1, 1, -1, 1).scale(r(4)).gcd(ExactPolynomial1D.ZERO));
		assertEquals(ExactPolynomial1D.ZERO,
				ExactPolynomial1D.ZERO.gcd(ExactPolynomial1D.ZERO));
		assertEquals(poly(-2, 1), ExactPolynomial1D.ZERO.gcd(poly(-6, 3)));
		Random random = new Random(SEED + 1);
		for (int i = 0; i < 50; i++) {
			ExactPolynomial1D common = randomPolynomial(random, 1 + random.nextInt(3));
			ExactPolynomial1D a = common.multiply(poly(random.nextInt(9) + 10, 1));
			ExactPolynomial1D b = common.multiply(poly(-random.nextInt(9) - 1, 1));
			ExactPolynomial1D gcd = a.gcd(b);
			assertEquals(common.monic(), gcd);
			assertEquals(ExactRational2D.ONE, gcd.leadingCoefficient());
			assertTrue(a.divideAndRemainder(gcd)[1].isZero());
			assertTrue(b.divideAndRemainder(gcd)[1].isZero());
		}
	}

	@Test
	void evaluationAndSignsAreExact() {
		ExactPolynomial1D cubic = ExactPolynomial1D.of(r(1, 2), r(-3), r(0), r(2));

		assertEquals(r(-3, 4), cubic.evaluate(r(1, 2)));
		assertEquals(-1, cubic.signAt(r(1, 2)));
		assertEquals(1, cubic.signAt(r(0)));
		assertEquals(0, poly(-1, 0, 1).signAt(r(-1)));
		assertEquals(1, cubic.signAtPositiveInfinity());
		assertEquals(-1, cubic.signAtNegativeInfinity());
		assertEquals(-1, poly(1, 0, -1).signAtNegativeInfinity());
		assertEquals(-1, poly(1, 0, -1).signAtPositiveInfinity());
		assertEquals(0, ExactPolynomial1D.ZERO.signAtPositiveInfinity());
		assertEquals(0, ExactPolynomial1D.ZERO.signAtNegativeInfinity());
		assertEquals(ExactRational2D.ZERO, ExactPolynomial1D.ZERO.evaluate(r(3)));
	}

	@Test
	void equalityHashAndTextAreDeterministic() {
		ExactPolynomial1D first = ExactPolynomial1D.of(r(1), r(-1), r(3, 2));
		ExactPolynomial1D second = poly(2, -2, 3).scale(r(1, 2));

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		assertEquals("3/2*x^2 + -1/1*x + 1/1", first.toString());
		assertEquals("0", ExactPolynomial1D.ZERO.toString());
		assertEquals("1/1*x^3", poly(0, 0, 0, 1).toString());
		assertEquals(2, first.maxCoefficientBitLength());
		assertEquals(0, ExactPolynomial1D.ZERO.maxCoefficientBitLength());
	}

	@Test
	void primitiveIntegerFormKeepsEverySign() {
		ExactPolynomial1D rational = ExactPolynomial1D.of(r(1, 2), r(-3, 4), r(6));

		assertArrayEquals(new BigInteger[] {BigInteger.valueOf(2), BigInteger.valueOf(-3),
				BigInteger.valueOf(24)}, rational.primitiveIntegerCoefficients());
		assertArrayEquals(new BigInteger[] {BigInteger.valueOf(-1), BigInteger.valueOf(-2)},
				poly(-2, -4).primitiveIntegerCoefficients());
		Random random = new Random(SEED + 2);
		for (int i = 0; i < 300; i++) {
			ExactPolynomial1D p = randomPolynomial(random, random.nextInt(7));
			BigInteger[] integers = p.primitiveIntegerCoefficients();
			ExactRational2D x = ExactRational2D.of(random.nextInt(41) - 20,
					1 + random.nextInt(16));
			assertEquals(p.signAt(x), ExactPolynomial1D.signOfIntegerPolynomial(integers, x));
		}
	}

	private static void assertDivision(ExactPolynomial1D a, ExactPolynomial1D b) {
		ExactPolynomial1D[] division = a.divideAndRemainder(b);
		assertEquals(a, division[0].multiply(b).add(division[1]));
		assertTrue(division[1].degree() < b.degree());
	}

	private static ExactPolynomial1D randomPolynomial(Random random, int degree) {
		ExactRational2D[] coefficients = new ExactRational2D[degree + 1];
		for (int i = 0; i <= degree; i++) {
			coefficients[i] = ExactRational2D.of(random.nextInt(61) - 30, 1 + random.nextInt(12));
		}
		if (coefficients[degree].isZero()) {
			coefficients[degree] = ExactRational2D.ONE;
		}
		return ExactPolynomial1D.of(coefficients);
	}

	private static ExactPolynomial1D poly(long... coefficients) {
		ExactRational2D[] values = new ExactRational2D[coefficients.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = r(coefficients[i]);
		}
		return ExactPolynomial1D.of(values);
	}

	private static ExactRational2D r(long value) {
		return ExactRational2D.of(value);
	}

	private static ExactRational2D r(long numerator, long denominator) {
		return ExactRational2D.of(numerator, denominator);
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/** Exact trigonometric polynomials and their tangent half-angle form. */
final class TrigonometricPolynomial2DTest {
	private static final long SEED = 20261021L;
	private static final TrigonometricPolynomial2D C = TrigonometricPolynomial2D.cosine();
	private static final TrigonometricPolynomial2D S = TrigonometricPolynomial2D.sine();

	@Test
	void constructionAndTotalDegree() {
		assertEquals(0, constant(3, 2).totalDegree());
		assertEquals(1, C.totalDegree());
		assertEquals(1, S.totalDegree());
		assertEquals(-1, TrigonometricPolynomial2D.ZERO.totalDegree());
		assertEquals(5, TrigonometricPolynomial2D.monomial(ExactRational2D.ONE, 2, 3)
				.totalDegree());
		assertSame(TrigonometricPolynomial2D.ZERO,
				TrigonometricPolynomial2D.monomial(ExactRational2D.ZERO, 1, 1));
		assertTrue(TrigonometricPolynomial2D.constant(ExactRational2D.ZERO).isZero());
		assertThrows(IllegalArgumentException.class,
				() -> TrigonometricPolynomial2D.monomial(ExactRational2D.ONE, -1, 0));
	}

	@Test
	void ringOperationsAreExact() {
		TrigonometricPolynomial2D sum = C.add(S);
		TrigonometricPolynomial2D square = sum.multiply(sum);

		assertEquals(3, square.termCount());
		assertEquals(ExactRational2D.ONE, square.coefficient(2, 0));
		assertEquals(ExactRational2D.of(2), square.coefficient(1, 1));
		assertEquals(ExactRational2D.ONE, square.coefficient(0, 2));
		assertEquals(ExactRational2D.ZERO, square.coefficient(3, 0));
		assertTrue(square.subtract(square).isZero());
		assertTrue(sum.scale(ExactRational2D.ZERO).isZero());
		assertEquals(C.negate().add(C), TrigonometricPolynomial2D.ZERO);
		assertEquals(square, S.multiply(S).add(C.multiply(S).scale(ExactRational2D.of(2)))
				.add(C.multiply(C)));
		assertEquals(square.hashCode(), S.add(C).multiply(C.add(S)).hashCode());
		assertEquals("1/1*s^2 + 2/1*c*s + 1/1*c^2", square.toString());
		assertEquals("0", TrigonometricPolynomial2D.ZERO.toString());
	}

	@Test
	void evaluationIsExact() {
		ExactRational2D c = ExactRational2D.of(3, 5);
		ExactRational2D s = ExactRational2D.of(4, 5);

		assertEquals(ExactRational2D.ONE, C.multiply(C).add(S.multiply(S)).evaluate(c, s));
		assertEquals(ExactRational2D.of(24, 25), C.multiply(S).scale(ExactRational2D.of(2))
				.evaluate(c, s));
		assertEquals(ExactRational2D.of(3, 2), constant(3, 2).evaluate(c, s));
		assertEquals(ExactRational2D.ZERO, TrigonometricPolynomial2D.ZERO.evaluate(c, s));
	}

	@Test
	void tangentHalfAngleFormOfBasicPolynomials() {
		assertEquals(poly(1, 0, -1), C.toTangentHalfAngle());
		assertEquals(poly(0, 2), S.toTangentHalfAngle());
		assertEquals(poly(1, 0, 2, 0, 1), C.multiply(C).add(S.multiply(S))
				.toTangentHalfAngle());
		assertTrue(C.multiply(C).add(S.multiply(S)).subtract(constant(1, 1))
				.toTangentHalfAngle().isZero());
		assertEquals(poly(7), constant(7, 1).toTangentHalfAngle());
		assertEquals(poly(2), constant(1, 1).add(C).toTangentHalfAngle());
		assertTrue(TrigonometricPolynomial2D.ZERO.toTangentHalfAngle().isZero());
	}

	@Test
	void tangentHalfAngleFormMatchesTheRationalParameterization() {
		Random random = new Random(SEED);
		for (int trial = 0; trial < 60; trial++) {
			TrigonometricPolynomial2D q = randomPolynomial(random);
			if (q.isZero()) {
				continue;
			}
			ExactPolynomial1D p = q.toTangentHalfAngle();
			int n = q.totalDegree();
			assertTrue(p.degree() <= 2 * n);
			assertEquals(q.evaluate(ExactRational2D.of(-1), ExactRational2D.ZERO),
					p.coefficient(2 * n));
			for (int point = 0; point < 10; point++) {
				ExactRational2D u = ExactRational2D.of(random.nextInt(81) - 40,
						1 + random.nextInt(9));
				ExactRational2D square = u.multiply(u);
				ExactRational2D weight = ExactRational2D.ONE.add(square);
				ExactRational2D c = ExactRational2D.ONE.subtract(square).divide(weight);
				ExactRational2D s = u.add(u).divide(weight);
				ExactRational2D scale = ExactRational2D.ONE;
				for (int k = 0; k < n; k++) {
					scale = scale.multiply(weight);
				}
				assertEquals(scale.multiply(q.evaluate(c, s)), p.evaluate(u), q + " at " + u);
			}
		}
	}

	private static TrigonometricPolynomial2D randomPolynomial(Random random) {
		TrigonometricPolynomial2D q = constant(random.nextInt(21) - 10, 1 + random.nextInt(4));
		int terms = 1 + random.nextInt(6);
		for (int i = 0; i < terms; i++) {
			int cosinePower = random.nextInt(4);
			int sinePower = random.nextInt(4 - cosinePower);
			q = q.add(TrigonometricPolynomial2D.monomial(
					ExactRational2D.of(random.nextInt(41) - 20, 1 + random.nextInt(6)),
					cosinePower, sinePower));
		}
		return q;
	}

	private static TrigonometricPolynomial2D constant(long numerator, long denominator) {
		return TrigonometricPolynomial2D.constant(ExactRational2D.of(numerator, denominator));
	}

	private static ExactPolynomial1D poly(long... coefficients) {
		ExactRational2D[] values = new ExactRational2D[coefficients.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = ExactRational2D.of(coefficients[i]);
		}
		return ExactPolynomial1D.of(values);
	}
}

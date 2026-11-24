/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Exact structural facts: domain, spans, ownership, multiplicities, closure, weights. */
final class BSplineStructureTest {

	/** Cubic with an interior knot of multiplicity 2 (C^1) and one of multiplicity 3 (C^0). */
	private static BSplineData multipleKnots() {
		return BSplineTestData.data(3, new double[] {0, 0, 0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 3},
				new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
				new double[] {0, 2, -1, 3, 0, 2, -2, 1, 0}, null);
	}

	@Test
	void domainSpansAndInteriorKnotsAreExact() {
		BSplineData data = multipleKnots();
		assertEquals(0.0, data.domainStart());
		assertEquals(3.0, data.domainEnd());
		assertArrayEquals(new int[] {3, 5, 8}, data.spanKnotIndices());
		assertEquals(3, data.spanCount());
		List<BSplineInteriorKnot> interior = data.interiorKnots();
		assertEquals(2, interior.size());
		assertEquals(1.0, interior.get(0).value());
		assertEquals(2, interior.get(0).multiplicity());
		assertEquals(1, interior.get(0).continuityOrder());
		assertEquals(2.0, interior.get(1).value());
		assertEquals(3, interior.get(1).multiplicity());
		assertEquals(0, interior.get(1).continuityOrder());
		assertTrue(data.isSpanKnotIndex(5));
		assertFalse(data.isSpanKnotIndex(4));
		assertFalse(data.isSpanKnotIndex(9));
		assertEquals(4, data.multiplicity(0.0));
		assertEquals(0, data.multiplicity(0.5));
	}

	@Test
	void ownershipFollowsSection33() {
		BSplineData data = multipleKnots();
		assertEquals(3, data.findSpan(0.0).spanKnotIndex());
		assertEquals(3, data.findSpan(-0.0).spanKnotIndex());
		assertEquals(3, data.findSpan(Math.nextDown(1.0)).spanKnotIndex());
		assertEquals(5, data.findSpan(1.0).spanKnotIndex());
		assertEquals(8, data.findSpan(2.0).spanKnotIndex());
		assertEquals(8, data.findSpan(3.0).spanKnotIndex());
		assertEquals(BSplineStatus.PARAMETER_OUTSIDE_DOMAIN,
				data.findSpan(Math.nextUp(3.0)).status());
		assertEquals(BSplineStatus.PARAMETER_OUTSIDE_DOMAIN,
				data.findSpan(-Double.MIN_VALUE).status());
		assertEquals(BSplineStatus.PARAMETER_NONFINITE, data.findSpan(Double.NaN).status());
		assertEquals(BSplineStatus.PARAMETER_NONFINITE,
				data.findSpan(Double.NEGATIVE_INFINITY).status());
		assertEquals(-1, data.findSpan(Double.NaN).spanKnotIndex());
		for (double u = 0.0; u <= 3.0; u += 0.0625) {
			assertEquals(ExactRationalBSplineReference.ownerSpan(data.knots(), 3, 8, u),
					data.findSpan(u).spanKnotIndex());
		}
	}

	@Test
	void closureIsExactEquality() {
		double[] knots = {0, 0, 0, 1, 2, 2, 2};
		BSplineData closed = BSplineTestData.data(2, knots, new double[] {1, 0, -1, 1},
				new double[] {0, 1, 0, 0}, null);
		assertTrue(closed.isExactlyClosed());
		assertEquals(0, closed.seamContinuityOrder());
		BSplineData near = BSplineTestData.data(2, knots, new double[] {1, 0, -1, 1 + 1e-15},
				new double[] {0, 1, 0, 0}, null);
		assertFalse(near.isExactlyClosed());
		assertEquals(-1, near.seamContinuityOrder());
		BSplineData signedZero = BSplineTestData.data(2, knots, new double[] {0.0, 0, -1, -0.0},
				new double[] {0, 1, 0, 0}, null);
		assertTrue(signedZero.isExactlyClosed());
	}

	@Test
	void weightFactsAreExact() {
		double[] knots = {0, 0, 0, 1, 1, 1};
		double[] xs = {1, 1, 0};
		double[] ys = {0, 1, 1};
		BSplineData implicit = BSplineTestData.data(2, knots, xs, ys, null);
		BSplineData ones = BSplineTestData.data(2, knots, xs, ys, new double[] {1, 1, 1});
		BSplineData twos = BSplineTestData.data(2, knots, xs, ys, new double[] {2, 2, 2});
		assertTrue(implicit.allWeightsEqual() && implicit.allWeightsUnit());
		assertSame(implicit, implicit.polynomialEquivalent());
		assertEquals(BSplineWeightMode.EXPLICIT, ones.weightMode());
		assertTrue(ones.allWeightsEqual() && ones.allWeightsUnit());
		assertTrue(twos.allWeightsEqual());
		assertFalse(twos.allWeightsUnit());
		assertEquals(BSplineWeightMode.IMPLICIT_UNIT,
				twos.polynomialEquivalent().weightMode());
		BSplineData mixed = BSplineTestData.data(2, knots, xs, ys,
				new double[] {1, Math.sqrt(0.5), 1});
		assertFalse(mixed.allWeightsEqual());
		assertNull(mixed.polynomialEquivalent());
		assertEquals(1.0, implicit.weight(1));
		assertEquals(2.0, twos.weight(1));
	}

	@Test
	void hodographIsDataOfDegreeMinusOne() {
		BSplineData data = multipleKnots();
		BSplineData first = data.hodograph().value();
		assertEquals(2, first.degree());
		assertEquals(8, first.controlPointCount());
		assertEquals(data.domainStart(), first.domainStart());
		assertEquals(data.domainEnd(), first.domainEnd());
		assertArrayEquals(new int[] {2, 4, 7}, first.spanKnotIndices());
		// Multiplicity 3 = degree + 1 in the hodograph: the derivative is discontinuous.
		assertEquals(-1, first.interiorKnots().get(1).continuityOrder());
		BSplineData third = first.hodograph().value().hodograph().value();
		assertEquals(0, third.degree());
		assertEquals(BSplineStatus.DEGREE_ZERO, third.hodograph().status());
		BSplineData rational = BSplineTestData.data(2, new double[] {0, 0, 0, 1, 1, 1},
				new double[] {1, 1, 0}, new double[] {0, 1, 1}, new double[] {1, 2, 1});
		assertEquals(BSplineStatus.EXPLICIT_WEIGHTS_UNSUPPORTED,
				rational.hodograph().status());
	}
}

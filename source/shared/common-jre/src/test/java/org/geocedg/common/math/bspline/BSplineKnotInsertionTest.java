/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.junit.jupiter.api.Test;

/** Boehm insertion against an exact rational insertion and the exact curve. */
final class BSplineKnotInsertionTest {

	private static Fraction[] fractions(double[] values) {
		Fraction[] result = new Fraction[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = Fraction.of(values[i]);
		}
		return result;
	}

	@Test
	void exactlyRepresentableInsertionEqualsTheExactInsertion() {
		// Dyadic data: every binary64 operation of this insertion is exact.
		double[] knots = {0, 0, 0, 1, 2, 3, 3, 3};
		double[] xs = {0, 4, 8, 4, 0};
		double[] ys = {0, 8, 0, -8, 4};
		BSplineData data = BSplineTestData.data(2, knots, xs, ys, null);
		BSplineData inserted = BSplineKnotInsertion.insert(data, 1.5).value();
		Fraction[][] exact = ExactRationalBSplineReference.insert(fractions(knots), 2,
				fractions(xs), fractions(ys), null, Fraction.of(1.5));
		assertEquals(knots.length + 1, inserted.knotCount());
		for (int i = 0; i < inserted.knotCount(); i++) {
			assertEquals(exact[0][i], Fraction.of(inserted.knot(i)));
		}
		for (int i = 0; i < inserted.controlPointCount(); i++) {
			assertEquals(exact[1][i], Fraction.of(inserted.x(i)));
			assertEquals(exact[2][i], Fraction.of(inserted.y(i)));
		}
		assertNull(exact[3]);
	}

	@Test
	void insertionPreservesTheCurveWithinTheRoundingBound() {
		Random random = new Random(BSplineTestData.SEED + 7);
		for (int p = 1; p <= 7; p++) {
			int count = 2 * p + 3;
			double[] knots = BSplineTestData.randomKnots(random, p, count, 0, 1);
			double[] xs = BSplineTestData.randomCoordinates(random, count, 0, 10);
			double[] ys = BSplineTestData.randomCoordinates(random, count, 0, 10);
			BSplineData data = BSplineTestData.data(p, knots, xs, ys, null);
			double t = 0.37 + 0.01 * p;
			if (data.multiplicity(t) >= p) {
				continue;
			}
			BSplineData inserted = BSplineKnotInsertion.insert(data, t).value();
			Fraction[][] exact = ExactRationalBSplineReference.insert(fractions(knots), p,
					fractions(xs), fractions(ys), null, Fraction.of(t));
			double scale = 0.0;
			for (int i = 0; i < count; i++) {
				scale = Math.max(scale, Math.hypot(xs[i], ys[i]));
			}
			// Each new point is one convex combination: 8 eps max|P| (one de Boor level).
			for (int i = 0; i < inserted.controlPointCount(); i++) {
				double error = ExactRationalBSplineReference.norm(
						Fraction.of(inserted.x(i)).sub(exact[1][i]),
						Fraction.of(inserted.y(i)).sub(exact[2][i]));
				assertTrue(error <= 9 * BSplineErrorCriteria.EPS * scale);
			}
			// Exact insertion keeps the exact curve: the reference agrees with itself exactly.
			double[] newKnots = inserted.knots();
			for (double u : BSplineTestData.parameters(data, random, 16)) {
				Fraction exactU = Fraction.of(u);
				int oldSpan = ExactRationalBSplineReference.ownerSpan(knots, p, count - 1, u);
				int newSpan = ExactRationalBSplineReference.ownerSpan(newKnots, p, count, u);
				Fraction[] before = ExactRationalBSplineReference.point(knots, p, xs, ys, null,
						oldSpan, exactU);
				Fraction[] after = exactCurve(exact, p, newSpan, exactU);
				assertEquals(before[0], after[0]);
				assertEquals(before[1], after[1]);
			}
		}
	}

	private static Fraction[] exactCurve(Fraction[][] exact, int p, int j, Fraction u) {
		// Cox-de Boor over fraction knots: reuse the reference through an exact basis built
		// from rational knots by evaluating the triangular recurrence directly.
		Fraction[] knots = exact[0];
		Fraction[] n = new Fraction[p + 1];
		n[0] = Fraction.ONE;
		for (int r = 1; r <= p; r++) {
			Fraction saved = Fraction.ZERO;
			for (int k = 0; k < r; k++) {
				Fraction right = knots[j + k + 1].sub(u);
				Fraction left = u.sub(knots[j + 1 - r + k]);
				Fraction temp = n[k].div(knots[j + k + 1].sub(knots[j + 1 - r + k]));
				n[k] = saved.add(right.mul(temp));
				saved = left.mul(temp);
			}
			n[r] = saved;
		}
		Fraction x = Fraction.ZERO;
		Fraction y = Fraction.ZERO;
		for (int k = 0; k <= p; k++) {
			x = x.add(n[k].mul(exact[1][j - p + k]));
			y = y.add(n[k].mul(exact[2][j - p + k]));
		}
		return new Fraction[] {x, y};
	}

	@Test
	void rationalInsertionIsHomogeneousAndKeepsCopiedPointsExactly() {
		double w = Math.sqrt(0.5);
		BSplineData data = BSplineTestData.data(2, new double[] {0, 0, 0, 1, 1, 2, 2, 2},
				new double[] {1, 1, 0, -1, -1}, new double[] {0, 1, 1, 1, 0},
				new double[] {1, w, 1, w, 1});
		BSplineData inserted = BSplineKnotInsertion.insert(data, 0.5).value();
		assertEquals(BSplineWeightMode.EXPLICIT, inserted.weightMode());
		assertEquals(6, inserted.controlPointCount());
		assertEquals(1.0, inserted.x(0));
		assertEquals(w, inserted.weight(4));
		assertEquals(-1.0, inserted.x(5));
		for (double u = 0.0; u <= 2.0; u += 0.125) {
			BSplinePoint before = BSplineEvaluation.point(data, u);
			BSplinePoint after = BSplineEvaluation.point(inserted, u);
			assertEquals(before.x(), after.x(), 1e-15);
			assertEquals(before.y(), after.y(), 1e-15);
		}
	}

	@Test
	void typedRefusals() {
		BSplineData data = BSplineTestData.data(2, new double[] {0, 0, 0, 1, 1, 2, 2, 2},
				new double[] {1, 1, 0, -1, -1}, new double[] {0, 1, 1, 1, 0}, null);
		assertEquals(BSplineStatus.KNOT_NOT_INTERIOR, BSplineKnotInsertion.insert(data, 0.0)
				.status());
		assertEquals(BSplineStatus.KNOT_NOT_INTERIOR, BSplineKnotInsertion.insert(data, 2.0)
				.status());
		assertEquals(BSplineStatus.KNOT_NOT_INTERIOR, BSplineKnotInsertion.insert(data, 3.0)
				.status());
		assertEquals(BSplineStatus.PARAMETER_NONFINITE,
				BSplineKnotInsertion.insert(data, Double.NaN).status());
		assertEquals(BSplineStatus.MULTIPLICITY_LIMIT, BSplineKnotInsertion.insert(data, 1.0)
				.status());
		BSplineData once = BSplineKnotInsertion.insert(data, 0.5).value();
		assertEquals(BSplineStatus.OK, BSplineKnotInsertion.insert(once, 0.5).status());
		BSplineData twice = BSplineKnotInsertion.insert(once, 0.5).value();
		assertEquals(BSplineStatus.MULTIPLICITY_LIMIT,
				BSplineKnotInsertion.insert(twice, 0.5).status());
		assertEquals(2, twice.multiplicity(0.5));
	}
}

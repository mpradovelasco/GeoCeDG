/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Random;

import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.junit.jupiter.api.Test;

/**
 * Span Bernstein form, local power form (ascending and descending), locality (BS-11) and
 * correctly rounded global coefficients; conditioning far from the origin (BS-20).
 */
final class BSplineSpanFormsTest {

	private static long bits(double value) {
		return Double.doubleToRawLongBits(value);
	}

	private static BSplineData randomData(Random random, int p, int count, double knotOffset,
			double knotWidth, double coordinateOffset) {
		double[] knots = BSplineTestData.randomKnots(random, p, count, knotOffset,
				knotOffset + knotWidth);
		return BSplineTestData.data(p, knots,
				BSplineTestData.randomCoordinates(random, count, coordinateOffset, 10),
				BSplineTestData.randomCoordinates(random, count, -coordinateOffset, 10), null);
	}

	@Test
	void bernsteinIsBitIdenticalToWholeDataInsertion() {
		Random random = new Random(BSplineTestData.SEED + 21);
		for (int p = 1; p <= 7; p++) {
			BSplineData data = randomData(random, p, 3 * p + 2, 0, 1, 0);
			for (int s = 0; s < data.spanCount(); s++) {
				int j = data.spanKnotIndex(s);
				double start = data.knot(j);
				double end = data.knot(j + 1);
				BSplineData refined = data;
				while (refined.multiplicity(start) < p) {
					refined = BSplineKnotInsertion.insert(refined, start).value();
				}
				while (refined.multiplicity(end) < p) {
					refined = BSplineKnotInsertion.insert(refined, end).value();
				}
				int jr = refined.findSpan(start).spanKnotIndex();
				BSplineSpanPolynomial b = BSplineSpanForms.bernstein(data, j).value();
				assertEquals(BSplineSpanPolynomial.Form.BERNSTEIN, b.form());
				for (int i = 0; i <= p; i++) {
					assertEquals(bits(refined.x(jr - p + i)), bits(b.x()[i]));
					assertEquals(bits(refined.y(jr - p + i)), bits(b.y()[i]));
				}
				assertNull(b.w());
				assertEquals(start, b.spanStart());
				assertEquals(end, b.spanEnd());
			}
		}
	}

	@Test
	void movingOneControlPointChangesOnlyItsSupport() {
		Random random = new Random(BSplineTestData.SEED + 22);
		int p = 4;
		int count = 16;
		double[] knots = BSplineTestData.randomKnots(random, p, count, 0, 1);
		double[] xs = BSplineTestData.randomCoordinates(random, count, 0, 10);
		double[] ys = BSplineTestData.randomCoordinates(random, count, 0, 10);
		BSplineData data = BSplineTestData.data(p, knots, xs, ys, null);
		int moved = 7;
		xs[moved] += 0.125;
		BSplineData changed = BSplineTestData.data(p, knots, xs, ys, null);
		for (int s = 0; s < data.spanCount(); s++) {
			int j = data.spanKnotIndex(s);
			double[] before = BSplineSpanForms.localPower(data, j).value().x();
			double[] after = BSplineSpanForms.localPower(changed, j).value().x();
			boolean inSupport = j - p <= moved && moved <= j;
			boolean identical = true;
			for (int k = 0; k <= p; k++) {
				identical &= bits(before[k]) == bits(after[k]);
			}
			assertEquals(!inSupport, identical, "span " + j);
		}
	}

	@Test
	void bernsteinAndLocalPowerReproduceTheExactCurve() {
		Random random = new Random(BSplineTestData.SEED + 23);
		StringBuilder report = new StringBuilder();
		for (int p = 1; p <= 7; p++) {
			double maxBernstein = 0.0;
			double maxLocal = 0.0;
			for (int family = 0; family < 3; family++) {
				BSplineData data = family == 0 ? randomData(random, p, 2 * p + 3, 0, 1, 0)
						: family == 1 ? randomData(random, p, 32, 1e6, 10, 0)
								: randomData(random, p, 2 * p + 3, 0, 1, 1e6);
				double[] knots = data.knots();
				double[] xs = data.xs();
				double[] ys = data.ys();
				for (int s = 0; s < data.spanCount(); s++) {
					int j = data.spanKnotIndex(s);
					BSplineSpanPolynomial b = BSplineSpanForms.bernstein(data, j).value();
					BSplineSpanPolynomial a = BSplineSpanForms.localPower(data, j).value();
					BSplineSpanPolynomial descending = BSplineSpanForms
							.localPowerDescending(data, j).value();
					assertEquals(BSplineSpanPolynomial.Form.LOCAL_POWER_DESCENDING,
							descending.form());
					for (int k = 0; k <= p; k++) {
						assertEquals(bits(a.x()[k]), bits(descending.x()[p - k]));
						assertEquals(bits(a.y()[k]), bits(descending.y()[p - k]));
					}
					double scale = 0.0;
					for (int i = j - p; i <= j; i++) {
						scale = Math.max(scale, Math.hypot(xs[i], ys[i]));
					}
					Fraction start = Fraction.of(knots[j]);
					Fraction width = Fraction.of(knots[j + 1]).sub(start);
					for (int q = 0; q <= 8; q++) {
						Fraction sLocal = Fraction.of(q).div(Fraction.of(8));
						Fraction u = start.add(width.mul(sLocal));
						Fraction[] exact = ExactRationalBSplineReference.point(knots, p, xs, ys,
								null, j, u);
						double bernsteinError = ExactRationalBSplineReference.norm(
								ExactRationalBSplineReference.bernsteinSum(fractions(b.x()), sLocal)
										.sub(exact[0]),
								ExactRationalBSplineReference.bernsteinSum(fractions(b.y()), sLocal)
										.sub(exact[1]));
						double localError = ExactRationalBSplineReference.norm(
								powerSum(a.x(), sLocal).sub(exact[0]),
								powerSum(a.y(), sLocal).sub(exact[1]));
						double rb = bernsteinError / (BSplineErrorCriteria.EPS * scale);
						double rl = localError / (BSplineErrorCriteria.EPS * scale);
						maxBernstein = Math.max(maxBernstein, rb);
						maxLocal = Math.max(maxLocal, rl);
						assertTrue(rb <= BSplineErrorCriteria.kBernstein(p), "bernstein " + rb);
						assertTrue(rl <= BSplineErrorCriteria.kLocal(p), "local " + rl);
					}
				}
			}
			report.append(String.format(Locale.ROOT,
					"BSPLINE-RATIOS span-forms p=%d maxBern=%.4f K_bern=%.0f maxLocal=%.4f"
							+ " K_loc=%.0f%n", p, maxBernstein, BSplineErrorCriteria.kBernstein(p),
					maxLocal, BSplineErrorCriteria.kLocal(p)));
		}
		System.out.print(report);
	}

	private static Fraction[] fractions(double[] values) {
		Fraction[] result = new Fraction[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = Fraction.of(values[i]);
		}
		return result;
	}

	private static Fraction powerSum(double[] coefficients, Fraction s) {
		Fraction sum = Fraction.ZERO;
		for (int k = coefficients.length - 1; k >= 0; k--) {
			sum = sum.mul(s).add(Fraction.of(coefficients[k]));
		}
		return sum;
	}

	@Test
	void localPowerFollowsTheFixedTransform() {
		Random random = new Random(BSplineTestData.SEED + 24);
		BSplineData data = randomData(random, 7, 20, 0, 1, 0);
		int j = data.spanKnotIndex(3);
		double[] b = BSplineSpanForms.bernstein(data, j).value().x();
		double[] a = BSplineSpanForms.localPower(data, j).value().x();
		for (int k = 0; k <= 7; k++) {
			double sum = ExactRationalBSplineReference.binomial(k, 0) * b[0];
			if (k % 2 == 1) {
				sum = -sum;
			}
			for (int i = 1; i <= k; i++) {
				double term = ExactRationalBSplineReference.binomial(k, i) * b[i];
				sum = (k - i) % 2 == 1 ? sum - term : sum + term;
			}
			assertEquals(bits(ExactRationalBSplineReference.binomial(7, k) * sum), bits(a[k]));
		}
	}

	@Test
	void globalCoefficientsAreCorrectlyRoundedExpansions() {
		Random random = new Random(BSplineTestData.SEED + 25);
		int checked = 0;
		for (int p = 1; p <= 7; p++) {
			for (int family = 0; family < 2; family++) {
				BSplineData data = family == 0 ? randomData(random, p, 2 * p + 2, -3, 7, 0)
						: randomData(random, p, 2 * p + 2, 1e6, 10, 0);
				for (int s = 0; s < data.spanCount(); s++) {
					int j = data.spanKnotIndex(s);
					BSplineSpanPolynomial a = BSplineSpanForms.localPower(data, j).value();
					BSplineGlobalPolynomial g = BSplineSpanForms.globalPower(data, j).value();
					assertTrue(g.allFinite());
					assertEquals(j, g.spanKnotIndex());
					Fraction[] ex = horner(a.x(), data.knot(j), data.knot(j + 1));
					Fraction[] ey = horner(a.y(), data.knot(j), data.knot(j + 1));
					for (int m = 0; m <= p; m++) {
						assertNearest(ex[m], g.x()[m]);
						assertNearest(ey[m], g.y()[m]);
						checked++;
					}
				}
			}
		}
		assertTrue(checked > 200);
	}

	/**
	 * Independent expansion: Horner composition of {@code sum a_k s^k} with
	 * {@code s = (u - start) / (end - start)} as polynomials in {@code u} over {@code Q}.
	 */
	private static Fraction[] horner(double[] local, double start, double end) {
		int p = local.length - 1;
		Fraction h = Fraction.of(end).sub(Fraction.of(start));
		Fraction slope = Fraction.ONE.div(h);
		Fraction intercept = Fraction.of(start).neg().div(h);
		Fraction[] poly = {Fraction.of(local[p])};
		for (int k = p - 1; k >= 0; k--) {
			Fraction[] next = new Fraction[poly.length + 1];
			for (int i = 0; i < next.length; i++) {
				next[i] = Fraction.ZERO;
			}
			for (int i = 0; i < poly.length; i++) {
				next[i] = next[i].add(poly[i].mul(intercept));
				next[i + 1] = next[i + 1].add(poly[i].mul(slope));
			}
			next[0] = next[0].add(Fraction.of(local[k]));
			poly = next;
		}
		return poly;
	}

	/**
	 * Correct rounding without any rounding routine: the double is at least as close to the
	 * exact value as both neighbours, and on a tie its significand is even.
	 */
	static void assertNearest(Fraction exact, double value) {
		assertTrue(Double.isFinite(value));
		Fraction distance = absolute(exact.sub(Fraction.of(value)));
		for (double neighbour : new double[] {Math.nextUp(value), Math.nextDown(value)}) {
			if (!Double.isFinite(neighbour)) {
				continue;
			}
			int comparison = distance.compareTo(absolute(exact.sub(Fraction.of(neighbour))));
			assertTrue(comparison <= 0, "not nearest: " + value);
			if (comparison == 0) {
				assertEquals(0L, Double.doubleToRawLongBits(value) & 1L, "tie not even");
			}
		}
	}

	private static Fraction absolute(Fraction value) {
		return value.signum() < 0 ? value.neg() : value;
	}

	@Test
	void farFromTheOriginTheLocalFormStaysAccurate() {
		// BS-20 style: knots [1e6, 1e6 + 10], p = 7; amplification ((|a| + h) / h)^p of a
		// global re-expansion versus at most 3^p for the local form.
		Random random = new Random(BSplineTestData.SEED + 26);
		BSplineData data = randomData(random, 7, 32, 1e6, 10, 0);
		int j = data.spanKnotIndex(data.spanCount() - 1);
		double h = data.knot(j + 1) - data.knot(j);
		double kappa = Math.pow((Math.abs(data.knot(j)) + h) / h, 7);
		BSplineGlobalPolynomial g = BSplineSpanForms.globalPower(data, j).value();
		double u = data.knot(j) + h / 3;
		double horner = 0.0;
		for (int k = 7; k >= 0; k--) {
			horner = horner * u + g.x()[k];
		}
		double reference = BSplineEvaluation.pointInSpan(data, j, u).x();
		System.out.printf(Locale.ROOT, "BSPLINE-CONDITIONING kappa=%.3e global-horner-error=%.3e"
				+ " (local form within K_loc)%n", kappa, Math.abs(horner - reference));
		assertTrue(kappa > 1e40);
	}

	@Test
	void rationalBernsteinIsHomogeneousAndGlobalNeedsPolynomialData() {
		double w = Math.sqrt(0.5);
		BSplineData quarter = BSplineTestData.data(2, new double[] {0, 0, 0, 1, 1, 1},
				new double[] {1, 1, 0}, new double[] {0, 1, 1}, new double[] {1, w, 1});
		BSplineSpanPolynomial b = BSplineSpanForms.bernstein(quarter, 2).value();
		assertArrayEquals(new double[] {1, w, 0}, b.x());
		assertArrayEquals(new double[] {0, w, 1}, b.y());
		assertArrayEquals(new double[] {1, w, 1}, b.w());
		assertEquals(BSplineStatus.EXPLICIT_WEIGHTS_UNSUPPORTED,
				BSplineSpanForms.globalPower(quarter, 2).status());
		assertEquals(BSplineStatus.SPAN_NOT_NON_DEGENERATE,
				BSplineSpanForms.bernstein(quarter, 1).status());
		assertEquals(BSplineStatus.SPAN_NOT_NON_DEGENERATE,
				BSplineSpanForms.localPowerDescending(quarter, 3).status());
		assertNull(quarter.polynomialEquivalent());
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * BS-16 style sweep and the adversarial families of matrix section 0.2 against the exact
 * rational reference, under E1 to E5 with the derived constants. The maximum observed ratios
 * are printed as {@code BSPLINE-RATIOS} lines (test standard output) and recorded in the
 * derivation note; an observation above a constant fails.
 */
final class BSplineErrorBoundSweepTest {

	private static void sweep(BSplineData data, Random random, int randomCount,
			BSplineErrorCriteria.Ratios ratios) {
		boolean rational = data.weightMode() == BSplineWeightMode.EXPLICIT;
		for (double u : BSplineTestData.parameters(data, random, randomCount)) {
			BSplinePoint point = BSplineEvaluation.point(data, u);
			BSplinePoint derivative = BSplineEvaluation.firstDerivative(data, u);
			check(data, point.spanKnotIndex(), u, point, derivative, ratios, rational);
			int left = data.findSpan(Math.nextDown(u)).spanKnotIndex();
			if (left >= 0 && left != point.spanKnotIndex()) {
				// One-sided limit from the left at an interior knot.
				check(data, left, u, BSplineEvaluation.pointInSpan(data, left, u),
						BSplineEvaluation.firstDerivativeInSpan(data, left, u), ratios, rational);
			}
		}
	}

	private static void check(BSplineData data, int span, double u, BSplinePoint point,
			BSplinePoint derivative, BSplineErrorCriteria.Ratios ratios, boolean rational) {
		if (rational) {
			BSplineErrorCriteria.checkRational(data, span, u, point, derivative, ratios);
		} else {
			BSplineErrorCriteria.checkPolynomial(data, span, u, point, derivative, ratios);
		}
	}

	@Test
	void polynomialDegreeSweep() {
		Random random = new Random(BSplineTestData.SEED);
		BSplineErrorCriteria.Ratios ratios = new BSplineErrorCriteria.Ratios("poly-sweep");
		for (int p = 1; p <= 7; p++) {
			for (int count : new int[] {p + 1, 2 * p + 1, 32}) {
				for (int variant = 0; variant < 2; variant++) {
					double[] knots = variant == 0 ? BSplineTestData.clampedUniform(p, count)
							: BSplineTestData.randomKnots(random, p, count, 0.0, 1.0);
					BSplineData data = BSplineTestData.data(p, knots,
							BSplineTestData.randomCoordinates(random, count, 0, 10),
							BSplineTestData.randomCoordinates(random, count, 0, 10), null);
					sweep(data, random, 64, ratios);
				}
			}
		}
		System.out.print(ratios.report(false));
	}

	@Test
	void polynomialAdversarialFamilies() {
		Random random = new Random(BSplineTestData.SEED + 1);
		BSplineErrorCriteria.Ratios offset = new BSplineErrorCriteria.Ratios("poly-offset-1e6");
		BSplineErrorCriteria.Ratios farKnots = new BSplineErrorCriteria.Ratios(
				"poly-knots-1e6");
		BSplineErrorCriteria.Ratios clustered = new BSplineErrorCriteria.Ratios(
				"poly-spacing-1e-6");
		BSplineErrorCriteria.Ratios nearCoincident = new BSplineErrorCriteria.Ratios(
				"poly-near-coincident");
		for (int p = 1; p <= 7; p++) {
			int count = 2 * p + 1;
			// Coordinates offset to 1e6.
			sweep(BSplineTestData.data(p, BSplineTestData.randomKnots(random, p, count, 0, 1),
					BSplineTestData.randomCoordinates(random, count, 1e6, 10),
					BSplineTestData.randomCoordinates(random, count, -1e6, 10), null), random, 64,
					offset);
			// Knots far from the origin: [1e6, 1e6 + 10], with coordinates also offset.
			double[] far = BSplineTestData.clampedUniform(p, 32);
			for (int i = 0; i < far.length; i++) {
				far[i] = 1e6 + far[i] * (10.0 / far[far.length - 1]);
			}
			sweep(BSplineTestData.data(p, far,
					BSplineTestData.randomCoordinates(random, 32, 1e6, 10),
					BSplineTestData.randomCoordinates(random, 32, 0, 10), null), random, 64,
					farKnots);
			// Knot spacings down to 1e-6 of the domain.
			sweep(BSplineTestData.data(p, clusteredKnots(p, count),
					BSplineTestData.randomCoordinates(random, count, 0, 10),
					BSplineTestData.randomCoordinates(random, count, 0, 10), null), random, 64,
					clustered);
			// Near-coincident consecutive control points.
			double[] xs = BSplineTestData.randomCoordinates(random, count, 0, 10);
			double[] ys = BSplineTestData.randomCoordinates(random, count, 0, 10);
			for (int i = 1; i < count; i += 2) {
				xs[i] = xs[i - 1] + 1e-12;
				ys[i] = ys[i - 1] - 1e-12;
			}
			sweep(BSplineTestData.data(p, BSplineTestData.randomKnots(random, p, count, 0, 1),
					xs, ys, null), random, 64, nearCoincident);
		}
		System.out.print(offset.report(false) + farKnots.report(false)
				+ clustered.report(false) + nearCoincident.report(false));
	}

	/** Clamped knots on [0, 1] whose interior values are 0.5 + k 1e-6. */
	private static double[] clusteredKnots(int p, int count) {
		int n = count - 1;
		double[] knots = new double[n + p + 2];
		for (int i = 0; i <= p; i++) {
			knots[n + 1 + i] = 1.0;
		}
		for (int k = 0; k < n - p; k++) {
			knots[p + 1 + k] = 0.5 + k * 1e-6;
		}
		return knots;
	}

	@Test
	void rationalSweepAndAdversarialWeights() {
		Random random = new Random(BSplineTestData.SEED + 2);
		BSplineErrorCriteria.Ratios ordinary = new BSplineErrorCriteria.Ratios("rat-sweep");
		BSplineErrorCriteria.Ratios ratio = new BSplineErrorCriteria.Ratios("rat-ratio-1e6");
		BSplineErrorCriteria.Ratios offset = new BSplineErrorCriteria.Ratios("rat-offset-1e6");
		for (int p = 1; p <= 7; p++) {
			for (int count : new int[] {p + 1, 2 * p + 1, 32}) {
				double[] ws = new double[count];
				for (int i = 0; i < count; i++) {
					ws[i] = Math.pow(2.0, 4.0 * random.nextDouble() - 2.0);
				}
				sweep(BSplineTestData.data(p,
						BSplineTestData.randomKnots(random, p, count, 0, 1),
						BSplineTestData.randomCoordinates(random, count, 0, 10),
						BSplineTestData.randomCoordinates(random, count, 0, 10), ws), random, 64,
						ordinary);
			}
			int count = 2 * p + 1;
			double[] wide = new double[count];
			for (int i = 0; i < count; i++) {
				wide[i] = i % 2 == 0 ? 1.0 : 1e6 * (0.5 + 0.5 * random.nextDouble());
			}
			sweep(BSplineTestData.data(p, BSplineTestData.randomKnots(random, p, count, 0, 1),
					BSplineTestData.randomCoordinates(random, count, 0, 10),
					BSplineTestData.randomCoordinates(random, count, 0, 10), wide), random, 64,
					ratio);
			double[] ws = new double[count];
			for (int i = 0; i < count; i++) {
				ws[i] = 0.5 + random.nextDouble();
			}
			sweep(BSplineTestData.data(p, clusteredKnots(p, count),
					BSplineTestData.randomCoordinates(random, count, 1e6, 10),
					BSplineTestData.randomCoordinates(random, count, -1e6, 10), ws), random, 64,
					offset);
		}
		System.out.print(ordinary.report(true) + ratio.report(true) + offset.report(true));
	}
}

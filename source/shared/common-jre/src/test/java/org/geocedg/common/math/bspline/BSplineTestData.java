/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Deterministic test data for the B-spline library tests. */
final class BSplineTestData {

	/** Fixed seed of every random family (recorded in the derivation note). */
	static final long SEED = 20261124L;
	static final BSplineLimits LIMITS = BSplineLimits.of(BSplineLimits.ABSOLUTE_MAX_DEGREE,
			4096);

	private BSplineTestData() {
	}

	static BSplineData data(int p, double[] knots, double[] xs, double[] ys, double[] ws) {
		BSplineValidation validation = BSplineData.validate(p, knots, xs, ys, ws, LIMITS);
		assertTrue(validation.isValid(), () -> "rejected: " + validation.cause());
		return validation.data();
	}

	/** Rule clamped-uniform/v1: p + 1 zeros, 1..n-p, p + 1 copies of n - p + 1. */
	static double[] clampedUniform(int p, int pointCount) {
		int n = pointCount - 1;
		double[] knots = new double[n + p + 2];
		for (int i = 0; i < knots.length; i++) {
			knots[i] = Math.min(Math.max(i - p, 0), n - p + 1);
		}
		return knots;
	}

	/**
	 * Clamped knots on {@code [a, b]} with random interior values; with probability 1/5 an
	 * interior value repeats the previous one while the multiplicity stays at most {@code p}.
	 */
	static double[] randomKnots(Random random, int p, int pointCount, double a, double b) {
		int n = pointCount - 1;
		int interior = n - p;
		double[] values = new double[interior];
		for (int i = 0; i < interior; i++) {
			values[i] = a + (b - a) * (0.02 + 0.96 * random.nextDouble());
		}
		Arrays.sort(values);
		int run = 1;
		for (int i = 1; i < interior; i++) {
			if (run < p && random.nextInt(5) == 0) {
				values[i] = values[i - 1];
				run++;
			} else {
				run = values[i] == values[i - 1] ? run + 1 : 1;
			}
		}
		double[] knots = new double[n + p + 2];
		for (int i = 0; i <= p; i++) {
			knots[i] = a;
			knots[n + 1 + i] = b;
		}
		System.arraycopy(values, 0, knots, p + 1, interior);
		return knots;
	}

	static double[] randomCoordinates(Random random, int count, double offset, double radius) {
		double[] values = new double[count];
		for (int i = 0; i < count; i++) {
			values[i] = offset + radius * (2.0 * random.nextDouble() - 1.0);
		}
		return values;
	}

	/**
	 * Parameters of a sweep: every distinct knot value of the domain, every span midpoint
	 * and {@code randomCount} random parameters (fixed generator).
	 */
	static List<Double> parameters(BSplineData data, Random random, int randomCount) {
		List<Double> result = new ArrayList<>();
		double previous = Double.NaN;
		for (int i = data.degree(); i <= data.controlPointCount(); i++) {
			double knot = data.knot(i);
			if (knot != previous) {
				result.add(knot);
				previous = knot;
			}
		}
		for (int s = 0; s < data.spanCount(); s++) {
			int j = data.spanKnotIndex(s);
			result.add(data.knot(j) + (data.knot(j + 1) - data.knot(j)) / 2.0);
		}
		double a = data.domainStart();
		double b = data.domainEnd();
		for (int i = 0; i < randomCount; i++) {
			result.add(Math.min(b, a + (b - a) * random.nextDouble()));
		}
		return result;
	}

	/**
	 * Straightforward polynomial de Boor written for the tests (textbook form, convex
	 * combination {@code (1 - alpha) d_{i-1} + alpha d_i}); BS-15 compares it bit for bit.
	 */
	static double[] textbookDeBoor(int p, double[] knots, double[] xs, double[] ys, int j,
			double u) {
		double[] dx = new double[p + 1];
		double[] dy = new double[p + 1];
		for (int i = 0; i <= p; i++) {
			dx[i] = xs[j - p + i];
			dy[i] = ys[j - p + i];
		}
		for (int r = 1; r <= p; r++) {
			for (int i = p; i >= r; i--) {
				double alpha = (u - knots[j - p + i]) / (knots[j + 1 + i - r] - knots[j - p + i]);
				dx[i] = (1.0 - alpha) * dx[i - 1] + alpha * dx[i];
				dy[i] = (1.0 - alpha) * dy[i - 1] + alpha * dy[i];
			}
		}
		return new double[] {dx[p], dy[p]};
	}
}

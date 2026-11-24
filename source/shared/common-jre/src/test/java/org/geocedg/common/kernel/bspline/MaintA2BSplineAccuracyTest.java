/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineEvaluation;
import org.geocedg.common.math.bspline.BSplinePoint;
import org.geocedg.common.math.bspline.ExactRationalBSplineReference;
import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2, BS-3, BS-15, BS-16 and BS-20 (matrix A-N1 sections 0.1 and 0.2): the
 * kernel evaluator against the exact rational reference of the library's tests, with the
 * constants derived for the library's operation sequence ({@code K_pos(p) = 8p + 1},
 * {@code K_der(p) = 8p - 3}, {@code K_loc(p) = (17p - 14) 3^p + 1}; derivation note of
 * MAINT-A-DXF0). The kernel adds no floating operation: every point and derivative is the
 * library's value bit for bit, which this test asserts first. Observed maxima and the
 * amplifications {@code kappa_j} are printed as {@code A2-OBSERVED} lines for the report.
 */
public class MaintA2BSplineAccuracyTest {
	private static final double EPS = 0x1p-53;
	private static final long SEED = 20261124L;
	private static final String BRANCH = "bspline-2d/main";

	private static BSplineData data(int p, double[] knots, double[] xs, double[] ys) {
		var validation = BSplineData.validate(p, knots, xs, ys, null,
				BSplineDefinition2D.LIMITS);
		assertTrue(validation.isValid(), String.valueOf(validation.cause()));
		return validation.data();
	}

	private static double[] randomKnots(Random random, int p, int count, double a,
			double b) {
		double[] knots = new double[count + p + 1];
		int interior = count - p - 1;
		double[] inner = new double[interior];
		for (int i = 0; i < interior; i++) {
			inner[i] = a + (b - a) * random.nextDouble();
		}
		java.util.Arrays.sort(inner);
		for (int i = 0; i < knots.length; i++) {
			knots[i] = i <= p ? a : i <= p + interior ? inner[i - p - 1] : b;
		}
		return knots;
	}

	private static double[] coordinates(Random random, int count, double offset,
			double radius) {
		double[] values = new double[count];
		for (int i = 0; i < count; i++) {
			values[i] = offset + radius * (2 * random.nextDouble() - 1);
		}
		return values;
	}

	private static double norm(double x, double y) {
		return Math.hypot(x, y);
	}

	/** Worst ratios error / (eps S) over a family. */
	private static final class Observed {
		private double position;
		private double derivative;
		private double local;
		private double kappa = 1;
		private double globalEvaluation;
	}

	private static void check(String family, BSplineData data, Random random,
			Observed observed) {
		int p = data.degree();
		int n = data.controlPointCount() - 1;
		double[] knots = data.knots();
		double[] xs = data.xs();
		double[] ys = data.ys();
		BSplineSemanticEvaluator2D evaluator = new BSplineSemanticEvaluator2D(BRANCH, data);
		List<Double> parameters = new ArrayList<>();
		for (int s = 0; s < data.spanCount(); s++) {
			double lower = knots[data.spanKnotIndex(s)];
			double upper = knots[data.spanKnotIndex(s) + 1];
			parameters.add(lower);
			parameters.add(lower + (upper - lower) / 2);
		}
		parameters.add(data.domainEnd());
		for (int i = 0; i < 8; i++) {
			parameters.add(data.domainStart()
					+ (data.domainEnd() - data.domainStart()) * random.nextDouble());
		}
		LocusInterval2D domain = new LocusInterval2D(data.domainStart(), data.domainEnd(),
				true, true);
		LocusBranch2D branch = null;
		for (double u : parameters) {
			BSplinePoint point = BSplineEvaluation.point(data, u);
			BSplinePoint derivative = BSplineEvaluation.firstDerivative(data, u);
			if (!data.isExactlyClosed() || u != data.domainEnd()) {
				var evaluation = evaluator.evaluate(null, branch, u, null);
				assertEquals(point.x(), evaluation.getPoint().getX(), 0, family);
				assertEquals(point.y(), evaluation.getPoint().getY(), 0, family);
				LocusDifferentialEvaluation2D differential =
						evaluator.evaluateDifferential(null, BRANCH, u, null);
				assertEquals(derivative.x(), differential.getDerivativeX(), 0, family);
				assertEquals(derivative.y(), differential.getDerivativeY(), 0, family);
			}
			int j = ExactRationalBSplineReference.ownerSpan(knots, p, n, u);
			Fraction exactU = Fraction.of(u);
			Fraction[] exact = ExactRationalBSplineReference.point(knots, p, xs, ys, null, j,
					exactU);
			Fraction[] exactDerivative = ExactRationalBSplineReference.derivative(knots, p,
					xs, ys, null, j, exactU);
			double sPos = 0;
			for (int i = j - p; i <= j; i++) {
				sPos = Math.max(sPos, norm(xs[i], ys[i]));
			}
			double sDer = 0;
			for (int i = j - p; i <= j - 1; i++) {
				sDer = Math.max(sDer, p * (norm(xs[i + 1], ys[i + 1]) + norm(xs[i], ys[i]))
						/ (knots[i + p + 1] - knots[i + 1]));
			}
			double errorPosition = norm(Fraction.of(point.x()).sub(exact[0]).approx(),
					Fraction.of(point.y()).sub(exact[1]).approx());
			double errorDerivative = norm(Fraction.of(derivative.x())
					.sub(exactDerivative[0]).approx(), Fraction.of(derivative.y())
							.sub(exactDerivative[1]).approx());
			if (sPos > 0) {
				double ratio = errorPosition / (EPS * sPos);
				observed.position = Math.max(observed.position, ratio);
				assertTrue(ratio <= 8 * p + 1, family + " E1 u=" + u + " ratio " + ratio);
			}
			if (sDer > 0) {
				double ratio = errorDerivative / (EPS * sDer);
				observed.derivative = Math.max(observed.derivative, ratio);
				assertTrue(ratio <= 8 * p - 3, family + " E3 u=" + u + " ratio " + ratio);
			}
		}
		checkLocal(family, data, evaluator, observed);
	}

	/** Local span coefficients against the exact curve at dyadic local parameters (BS-20). */
	private static void checkLocal(String family, BSplineData data,
			BSplineSemanticEvaluator2D evaluator, Observed observed) {
		int p = data.degree();
		int n = data.controlPointCount() - 1;
		double[] knots = data.knots();
		double kLocal = (17.0 * p - 14) * Math.pow(3, p) + 1;
		for (int s = 0; s < data.spanCount(); s++) {
			int j = data.spanKnotIndex(s);
			Fraction lower = Fraction.of(knots[j]);
			Fraction width = Fraction.of(knots[j + 1]).sub(lower);
			observed.kappa = Math.max(observed.kappa, BSplineSemanticEvaluator2D
					.reexpansionAmplification(knots[j], knots[j + 1], p));
			double[][] local = evaluator.getLocalPolynomialCoordinateCoefficients(BRANCH, s);
			double[][] global = evaluator.getPolynomialCoordinateCoefficients(BRANCH, s);
			for (int c = 0; c < 2; c++) {
				Fraction[] exactGlobal = expand(local[c], lower, width);
				for (int k = 0; k < exactGlobal.length; k++) {
					assertTrue(correctlyRounded(global[c][k], exactGlobal[k]),
							family + " global coefficient " + k);
				}
			}
			double sPos = 0;
			for (int i = j - p; i <= j; i++) {
				sPos = Math.max(sPos, norm(data.x(i), data.y(i)));
			}
			for (int step = 0; step <= 4; step++) {
				Fraction t = Fraction.of(BigInteger.valueOf(step), BigInteger.valueOf(4));
				Fraction[] exact = ExactRationalBSplineReference.point(knots, p, data.xs(),
						data.ys(), null, j, lower.add(width.mul(t)));
				double error = norm(horner(local[0], t).sub(exact[0]).approx(),
						horner(local[1], t).sub(exact[1]).approx());
				Fraction u = lower.add(width.mul(t));
				double globalError = norm(hornerU(global[0], u).sub(exact[0]).approx(),
						hornerU(global[1], u).sub(exact[1]).approx());
				if (sPos > 0) {
					observed.globalEvaluation = Math.max(observed.globalEvaluation,
							globalError / (EPS * sPos));
					double ratio = error / (EPS * sPos);
					observed.local = Math.max(observed.local, ratio);
					assertTrue(ratio <= kLocal, family + " local span " + s + " ratio " + ratio);
				}
			}
		}
		assertEquals(n + 1, data.controlPointCount());
	}

	/** Exact descending global coefficients of the local polynomial, u = lower + width s. */
	private static Fraction[] expand(double[] localDescending, Fraction lower, Fraction width) {
		int p = localDescending.length - 1;
		Fraction[] result = new Fraction[p + 1];
		java.util.Arrays.fill(result, Fraction.ZERO);
		Fraction[] power = {Fraction.ONE};
		Fraction[] linear = {lower.neg().div(width), Fraction.ONE.div(width)};
		for (int i = 0; i <= p; i++) {
			Fraction coefficient = Fraction.of(localDescending[p - i]);
			for (int k = 0; k < power.length; k++) {
				result[p - k] = result[p - k].add(coefficient.mul(power[k]));
			}
			Fraction[] next = new Fraction[power.length + 1];
			java.util.Arrays.fill(next, Fraction.ZERO);
			for (int k = 0; k < power.length; k++) {
				next[k] = next[k].add(power[k].mul(linear[0]));
				next[k + 1] = next[k + 1].add(power[k].mul(linear[1]));
			}
			power = next;
		}
		return result;
	}

	private static boolean correctlyRounded(double value, Fraction exact) {
		if (!Double.isFinite(value)) {
			return false;
		}
		Fraction distance = abs(Fraction.of(value).sub(exact));
		return distance.compareTo(abs(Fraction.of(Math.nextUp(value)).sub(exact))) <= 0
				&& distance.compareTo(abs(Fraction.of(Math.nextDown(value)).sub(exact))) <= 0;
	}

	private static Fraction abs(Fraction value) {
		return value.signum() < 0 ? value.neg() : value;
	}

	private static Fraction hornerU(double[] descending, Fraction u) {
		return horner(descending, u);
	}

	private static Fraction horner(double[] descending, Fraction t) {
		Fraction value = Fraction.ZERO;
		for (double coefficient : descending) {
			value = value.mul(t).add(Fraction.of(coefficient));
		}
		return value;
	}

	private static void report(String family, Observed observed) {
		System.out.println("A2-OBSERVED " + family + " maxE1/(eps S_pos)=" + observed.position
				+ " maxE3/(eps S_der)=" + observed.derivative + " maxLocal/(eps S_pos)="
				+ observed.local + " globalRouteEvaluation/(eps S_pos)="
				+ observed.globalEvaluation + " kappa_max_observed=" + observed.kappa);
	}

	@Test
	void bs16DegreeSweepWithUniformAndRandomKnots() {
		Random random = new Random(SEED);
		for (int p = 1; p <= 7; p++) {
			for (int count : new int[] {p + 1, 2 * p + 1, 32}) {
				Observed observed = new Observed();
				double[] xs = coordinates(random, count, 0, 10);
				double[] ys = coordinates(random, count, 0, 10);
				check("uniform p=" + p + " n+1=" + count,
						data(p, BSplineDefinition2D.clampedUniform(p, count), xs, ys), random,
						observed);
				check("random p=" + p + " n+1=" + count,
						data(p, randomKnots(random, p, count, -3, 5), xs, ys), random, observed);
				report("BS-16 p=" + p + " n+1=" + count, observed);
			}
		}
	}

	@Test
	void bs3AndAdversarialFamiliesStayWithinTheDerivedConstants() {
		Random random = new Random(SEED + 1);
		Observed offset = new Observed();
		Observed spacing = new Observed();
		Observed coincident = new Observed();
		for (int p = 3; p <= 7; p += 2) {
			int count = 2 * p + 3;
			check("offset 1e6 p=" + p, data(p, BSplineDefinition2D.clampedUniform(p, count),
					coordinates(random, count, 1E6, 5), coordinates(random, count, -1E6, 5)),
					random, offset);
			double[] knots = BSplineDefinition2D.clampedUniform(p, count);
			knots[p + 1] = knots[p] + 1E-6 * knots[knots.length - 1];
			check("spacing 1e-6 p=" + p, data(p, knots, coordinates(random, count, 0, 3),
					coordinates(random, count, 0, 3)), random, spacing);
			double[] xs = coordinates(random, count, 0, 3);
			double[] ys = coordinates(random, count, 0, 3);
			xs[2] = Math.nextUp(xs[1]);
			ys[2] = ys[1];
			check("near-coincident p=" + p, data(p, randomKnots(random, p, count, 0, 2), xs,
					ys), random, coincident);
		}
		report("offset 1e6", offset);
		report("spacing 1e-6", spacing);
		report("near-coincident", coincident);
	}

	@Test
	void bs20LocalCoefficientsStayAccurateWhereGlobalOnesWouldNot() {
		Random random = new Random(SEED + 2);
		Observed far = new Observed();
		for (int p : new int[] {3, 5, 7}) {
			int count = p + 11;
			double[] knots = BSplineDefinition2D.clampedUniform(p, count);
			for (int i = 0; i < knots.length; i++) {
				knots[i] += 1E6;
			}
			check("knots near 1e6 p=" + p, data(p, knots, coordinates(random, count, 0, 4),
					coordinates(random, count, 0, 4)), random, far);
		}
		report("BS-20 knots [1e6, 1e6+11]", far);
		Observed wide = new Observed();
		int p = 7;
		int count = 20;
		double[] knots = randomKnots(random, p, count, -1E8, 1E8);
		knots[p + 1] = knots[p] + 1;
		check("wide [-1e8, 1e8] p=7", data(p, knots, coordinates(random, count, 0, 4),
				coordinates(random, count, 0, 4)), random, wide);
		report("BS-20 wide range", wide);
		Observed cubic128 = new Observed();
		check("cubic 128 points", data(3, BSplineDefinition2D.clampedUniform(3, 128),
				coordinates(random, 128, 0, 50), coordinates(random, 128, 0, 50)), random,
				cubic128);
		report("BS-20 cubic, 128 points (125 spans)", cubic128);
		assertTrue(far.kappa > 0x1p16 && wide.kappa > 0x1p16);
	}
}

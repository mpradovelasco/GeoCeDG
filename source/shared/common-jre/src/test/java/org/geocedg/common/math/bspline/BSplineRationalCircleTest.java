/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.junit.jupiter.api.Test;

/**
 * Rational circle fixtures Q and H of matrix section 2.1 at library level, with the three
 * checks kept separate: (a) library against the exact rational evaluation of the binary64
 * data; (b) the exact-weight curve against the circle oracle; (c) the weight-rounding
 * discrepancy between the binary64 data and the oracle, reported and never charged to the
 * evaluator.
 */
final class BSplineRationalCircleTest {

	private static final MathContext MC = new MathContext(80, RoundingMode.HALF_EVEN);
	/** The written weight 0.7071067811865476 reads as this binary64 value. */
	private static final double W = Double.parseDouble("0.7071067811865476");

	private static BSplineData quarter() {
		return BSplineTestData.data(2, new double[] {0, 0, 0, 1, 1, 1}, new double[] {1, 1, 0},
				new double[] {0, 1, 1}, new double[] {1, W, 1});
	}

	private static BSplineData semicircle() {
		return BSplineTestData.data(2, new double[] {0, 0, 0, 1, 1, 2, 2, 2},
				new double[] {1, 1, 0, -1, -1}, new double[] {0, 1, 1, 1, 0},
				new double[] {1, W, 1, W, 1});
	}

	private static List<Double> quarterParameters() {
		List<Double> values = new ArrayList<>(List.of(0.0, 0.125, 0.25, 0.5, 0.75, 0.875, 1.0));
		Random random = new Random(BSplineTestData.SEED);
		for (int i = 0; i < 64; i++) {
			values.add(random.nextDouble());
		}
		return values;
	}

	/** Oracle point of one quarter arc at t: ((1 - r^2) / (1 + r^2), 2r / (1 + r^2)). */
	private static BigDecimal[] oracle(BigDecimal t) {
		BigDecimal sqrt2 = BigDecimal.valueOf(2).sqrt(MC);
		BigDecimal r = t.divide(sqrt2.subtract(sqrt2.subtract(BigDecimal.ONE).multiply(t, MC),
				MC), MC);
		BigDecimal r2 = r.multiply(r, MC);
		BigDecimal d = BigDecimal.ONE.add(r2);
		return new BigDecimal[] {BigDecimal.ONE.subtract(r2).divide(d, MC),
				r.multiply(BigDecimal.valueOf(2)).divide(d, MC)};
	}

	/** Rational quadratic arc (1,0), (1,1), (0,1) with weights (1, w, 1) at t. */
	private static BigDecimal[] arc(BigDecimal t, BigDecimal w) {
		BigDecimal oneMinus = BigDecimal.ONE.subtract(t);
		BigDecimal a = oneMinus.multiply(oneMinus, MC);
		BigDecimal b = BigDecimal.valueOf(2).multiply(t).multiply(oneMinus, MC).multiply(w, MC);
		BigDecimal c = t.multiply(t, MC);
		BigDecimal d = a.add(b).add(c);
		return new BigDecimal[] {a.add(b).divide(d, MC), b.add(c).divide(d, MC)};
	}

	private static double distance(BigDecimal[] p, BigDecimal[] q) {
		return Math.hypot(p[0].subtract(q[0]).doubleValue(), p[1].subtract(q[1]).doubleValue());
	}

	@Test
	void writtenWeightReadsAsTheRecordedBinary64() {
		assertEquals(0x3FE6A09E667F3BCDL, Double.doubleToRawLongBits(W));
		BigDecimal readingRounding = new BigDecimal(W).subtract(
				new BigDecimal("0.7071067811865476")).abs();
		BigDecimal weightRounding = new BigDecimal(W).subtract(
				BigDecimal.valueOf(2).sqrt(MC).divide(BigDecimal.valueOf(2), MC)).abs();
		System.out.printf(Locale.ROOT, "BSPLINE-CIRCLE reading-rounding=%.3e"
				+ " weight-vs-sqrt2/2=%.3e%n", readingRounding.doubleValue(),
				weightRounding.doubleValue());
		assertTrue(readingRounding.compareTo(new BigDecimal("1e-16")) < 0);
	}

	@Test
	void quarterCircleThreeSeparateChecks() {
		BSplineData data = quarter();
		BSplineErrorCriteria.Ratios ratios = new BSplineErrorCriteria.Ratios("circle-Q");
		BigDecimal exactWeight = BigDecimal.valueOf(2).sqrt(MC).divide(BigDecimal.valueOf(2),
				MC);
		double oracleCheck = 0.0;
		double discrepancy = 0.0;
		for (double t : quarterParameters()) {
			BSplinePoint point = BSplineEvaluation.point(data, t);
			BSplinePoint derivative = BSplineEvaluation.firstDerivative(data, t);
			// (a) evaluator against the exact rational evaluation of the binary64 data.
			BSplineErrorCriteria.checkRational(data, point.spanKnotIndex(), t, point,
					derivative, ratios);
			BigDecimal exactT = new BigDecimal(t);
			// (b) exact-weight curve against the oracle.
			oracleCheck = Math.max(oracleCheck, distance(arc(exactT, exactWeight),
					oracle(exactT)));
			// (c) binary64 data (exact rational evaluation) against the oracle.
			Fraction[] exact = ExactRationalBSplineReference.point(data.knots(), 2, data.xs(),
					data.ys(), data.explicitWeights(), 2, Fraction.of(t));
			discrepancy = Math.max(discrepancy, distance(new BigDecimal[] {
					exact[0].toBigDecimal(MC), exact[1].toBigDecimal(MC)}, oracle(exactT)));
			radialIdentity(exact, Fraction.of(t));
		}
		assertTrue(oracleCheck < 1e-60, "oracle and exact-weight curve differ: " + oracleCheck);
		assertTrue(discrepancy < 1e-15);
		System.out.printf(Locale.ROOT, "BSPLINE-CIRCLE Q oracle-check=%.3e"
				+ " weight-rounding-discrepancy=%.3e%n", oracleCheck, discrepancy);
		System.out.print(ratios.report(true));
		assertEquals(1.0, BSplineEvaluation.point(data, 0.0).x());
		assertEquals(1.0, BSplineEvaluation.point(data, 1.0).y());
	}

	/** |C|^2 - 1 = 2 t^2 (1 - t)^2 (2 w^2 - 1) / D^2 exactly, with the binary64 w. */
	private static void radialIdentity(Fraction[] c, Fraction t) {
		Fraction w = Fraction.of(W);
		Fraction oneMinus = Fraction.ONE.sub(t);
		Fraction d = oneMinus.mul(oneMinus).add(Fraction.of(2).mul(t).mul(oneMinus).mul(w))
				.add(t.mul(t));
		Fraction left = c[0].mul(c[0]).add(c[1].mul(c[1])).sub(Fraction.ONE);
		Fraction right = Fraction.of(2).mul(t).mul(t).mul(oneMinus).mul(oneMinus)
				.mul(Fraction.of(2).mul(w).mul(w).sub(Fraction.ONE)).div(d.mul(d));
		assertEquals(right, left);
	}

	@Test
	void semicircleThreeSeparateChecksAndKnotBehaviour() {
		BSplineData data = semicircle();
		assertEquals(0, data.interiorKnots().get(0).continuityOrder());
		BSplineErrorCriteria.Ratios ratios = new BSplineErrorCriteria.Ratios("circle-H");
		BigDecimal exactWeight = BigDecimal.valueOf(2).sqrt(MC).divide(BigDecimal.valueOf(2),
				MC);
		List<Double> parameters = new ArrayList<>();
		for (double t : quarterParameters()) {
			parameters.add(t);
			parameters.add(1.0 + t);
		}
		for (int k : new int[] {10, 20, 30}) {
			parameters.add(1.0 - Math.scalb(1.0, -k));
			parameters.add(1.0 + Math.scalb(1.0, -k));
		}
		double oracleCheck = 0.0;
		double discrepancy = 0.0;
		for (double u : parameters) {
			BSplinePoint point = BSplineEvaluation.point(data, u);
			BSplineErrorCriteria.checkRational(data, point.spanKnotIndex(), u, point,
					BSplineEvaluation.firstDerivative(data, u), ratios);
			boolean second = u >= 1.0;
			BigDecimal t = new BigDecimal(second ? u - 1.0 : u);
			BigDecimal[] q = oracle(t);
			// Second span: Q rotated by +90 degrees, (x, y) -> (-y, x).
			BigDecimal[] o = second ? new BigDecimal[] {q[1].negate(), q[0]} : q;
			BigDecimal[] a = arc(t, exactWeight);
			BigDecimal[] curve = second ? new BigDecimal[] {a[1].negate(), a[0]} : a;
			oracleCheck = Math.max(oracleCheck, distance(curve, o));
			Fraction[] exact = ExactRationalBSplineReference.point(data.knots(), 2, data.xs(),
					data.ys(), data.explicitWeights(), point.spanKnotIndex(), Fraction.of(u));
			discrepancy = Math.max(discrepancy, distance(new BigDecimal[] {
					exact[0].toBigDecimal(MC), exact[1].toBigDecimal(MC)}, o));
		}
		assertTrue(oracleCheck < 1e-60);
		assertTrue(discrepancy < 1e-15);
		// u = 1 is owned by the right span and evaluates to P_2 = (0, 1).
		BSplinePoint top = BSplineEvaluation.point(data, 1.0);
		assertEquals(4, top.spanKnotIndex());
		assertEquals(0.0, top.x());
		assertEquals(1.0, top.y());
		// One-sided first derivatives at u = 1: exactly equal in the reference (observed C^1,
		// published C^0), each within E3 in the library.
		Fraction one = Fraction.of(1.0);
		Fraction[] left = ExactRationalBSplineReference.derivative(data.knots(), 2, data.xs(),
				data.ys(), data.explicitWeights(), 2, one);
		Fraction[] right = ExactRationalBSplineReference.derivative(data.knots(), 2, data.xs(),
				data.ys(), data.explicitWeights(), 4, one);
		assertEquals(left[0], right[0]);
		assertEquals(left[1], right[1]);
		assertEquals(Fraction.of(-2.0).mul(Fraction.of(W)), left[0]);
		BSplineErrorCriteria.checkRational(data, 2, 1.0, BSplineEvaluation.pointInSpan(data, 2,
				1.0), BSplineEvaluation.firstDerivativeInSpan(data, 2, 1.0), ratios);
		System.out.printf(Locale.ROOT, "BSPLINE-CIRCLE H oracle-check=%.3e"
				+ " weight-rounding-discrepancy=%.3e%n", oracleCheck, discrepancy);
		System.out.print(ratios.report(true));
	}
}

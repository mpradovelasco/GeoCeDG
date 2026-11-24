/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.junit.jupiter.api.Test;

/**
 * Evaluation: BS-15 bit identity, Bezier equivalence p = 1..7 (BS-17), ownership and
 * one-sided limits (BS-18), multiple knots (BS-4), degenerations (BS-6), reproducibility and
 * typed statuses.
 */
final class BSplineEvaluationTest {

	private static long bits(double value) {
		return Double.doubleToRawLongBits(value);
	}

	@Test
	void implicitWeightsAreBitIdenticalToPolynomialDeBoor() {
		Random random = new Random(BSplineTestData.SEED);
		int checked = 0;
		for (int p = 1; p <= 7; p++) {
			for (int count : new int[] {p + 1, 2 * p + 1, 32}) {
				for (int variant = 0; variant < 2; variant++) {
					double[] knots = variant == 0 ? BSplineTestData.clampedUniform(p, count)
							: BSplineTestData.randomKnots(random, p, count, -3.0, 5.0);
					double[] xs = BSplineTestData.randomCoordinates(random, count, 0, 10);
					double[] ys = BSplineTestData.randomCoordinates(random, count, 0, 10);
					BSplineData data = BSplineTestData.data(p, knots, xs, ys, null);
					BSplineData hodograph = data.hodograph().value();
					for (double u : BSplineTestData.parameters(data, random, 64)) {
						BSplinePoint point = BSplineEvaluation.point(data, u);
						double[] expected = BSplineTestData.textbookDeBoor(p, knots, xs, ys,
								point.spanKnotIndex(), u);
						assertEquals(bits(expected[0]), bits(point.x()));
						assertEquals(bits(expected[1]), bits(point.y()));
						BSplinePoint derivative = BSplineEvaluation.firstDerivative(data, u);
						BSplinePoint viaHodograph = BSplineEvaluation.point(hodograph, u);
						assertEquals(bits(viaHodograph.x()), bits(derivative.x()));
						assertEquals(bits(viaHodograph.y()), bits(derivative.y()));
						checked++;
					}
				}
			}
		}
		assertTrue(checked > 2000);
	}

	@Test
	void bezierEquivalenceForEveryDegree() {
		Random random = new Random(BSplineTestData.SEED + 17);
		BSplineErrorCriteria.Ratios ratios = new BSplineErrorCriteria.Ratios("bezier");
		for (int p = 1; p <= 7; p++) {
			double[] knots = BSplineTestData.clampedUniform(p, p + 1);
			double[] xs = BSplineTestData.randomCoordinates(random, p + 1, 0, 10);
			double[] ys = BSplineTestData.randomCoordinates(random, p + 1, 0, 10);
			BSplineData data = BSplineTestData.data(p, knots, xs, ys, null);
			Fraction[] bx = new Fraction[p + 1];
			Fraction[] by = new Fraction[p + 1];
			Fraction[] dbx = new Fraction[p];
			Fraction[] dby = new Fraction[p];
			for (int i = 0; i <= p; i++) {
				bx[i] = Fraction.of(xs[i]);
				by[i] = Fraction.of(ys[i]);
			}
			for (int i = 0; i < p; i++) {
				dbx[i] = bx[i + 1].sub(bx[i]).mul(Fraction.of(p));
				dby[i] = by[i + 1].sub(by[i]).mul(Fraction.of(p));
			}
			for (double u : BSplineTestData.parameters(data, random, 64)) {
				Fraction s = Fraction.of(u);
				BSplinePoint point = BSplineEvaluation.point(data, u);
				BSplinePoint derivative = BSplineEvaluation.firstDerivative(data, u);
				// Independent Bernstein reference (not Cox-de Boor).
				double positionError = ExactRationalBSplineReference.norm(
						Fraction.of(point.x())
								.sub(ExactRationalBSplineReference.bernsteinSum(bx, s)),
						Fraction.of(point.y())
								.sub(ExactRationalBSplineReference.bernsteinSum(by, s)));
				double derivativeError = ExactRationalBSplineReference.norm(
						Fraction.of(derivative.x())
								.sub(ExactRationalBSplineReference.bernsteinSum(dbx, s)),
						Fraction.of(derivative.y())
								.sub(ExactRationalBSplineReference.bernsteinSum(dby, s)));
				double sPos = 0.0;
				double sDer = 0.0;
				for (int i = 0; i <= p; i++) {
					sPos = Math.max(sPos, Math.hypot(xs[i], ys[i]));
				}
				for (int i = 0; i < p; i++) {
					sDer = Math.max(sDer, p * (Math.hypot(xs[i + 1], ys[i + 1])
							+ Math.hypot(xs[i], ys[i])));
				}
				assertTrue(positionError <= BSplineErrorCriteria.kPos(p)
						* BSplineErrorCriteria.EPS * sPos);
				assertTrue(derivativeError <= BSplineErrorCriteria.kDer(p)
						* BSplineErrorCriteria.EPS * sDer);
				BSplineErrorCriteria.checkPolynomial(data, point.spanKnotIndex(), u, point,
						derivative, ratios);
			}
			// u = a gives P_0 (opposite of Classic BezierCurve), exactly.
			assertEquals(xs[0], BSplineEvaluation.point(data, 0.0).x());
			assertEquals(ys[p], BSplineEvaluation.point(data, 1.0).y());
		}
		System.out.print(ratios.report(false));
	}

	@Test
	void endsAndMultiplicityPKnotsInterpolateExactly() {
		Random random = new Random(BSplineTestData.SEED + 3);
		double[] knots = {-2, -2, -2, -2, 0.3, 0.3, 0.3, 1.7, 4, 4, 4, 4};
		double[] xs = BSplineTestData.randomCoordinates(random, 8, 1e3, 50);
		double[] ys = BSplineTestData.randomCoordinates(random, 8, -7, 50);
		BSplineData data = BSplineTestData.data(3, knots, xs, ys, null);
		BSplinePoint start = BSplineEvaluation.point(data, -2.0);
		BSplinePoint end = BSplineEvaluation.point(data, 4.0);
		BSplinePoint middle = BSplineEvaluation.point(data, 0.3);
		assertEquals(bits(xs[0]), bits(start.x()));
		assertEquals(bits(ys[0]), bits(start.y()));
		assertEquals(bits(xs[7]), bits(end.x()));
		assertEquals(bits(ys[7]), bits(end.y()));
		// Multiplicity p: the curve passes through P_{j-p} with j = 6.
		assertEquals(bits(xs[3]), bits(middle.x()));
		assertEquals(bits(ys[3]), bits(middle.y()));
		assertEquals(6, middle.spanKnotIndex());
		BSplinePoint leftOfMiddle = BSplineEvaluation.pointInSpan(data, 3, 0.3);
		assertEquals(3, leftOfMiddle.spanKnotIndex());
		assertEquals(bits(xs[3]), bits(leftOfMiddle.x()));
	}

	@Test
	void oneSidedLimitsAgreeWithThePublishedContinuity() {
		Random random = new Random(BSplineTestData.SEED + 5);
		BSplineErrorCriteria.Ratios ratios = new BSplineErrorCriteria.Ratios("one-sided");
		double[] knots = {0, 0, 0, 0, 0.25, 0.5, 0.5, 0.75, 0.75, 0.75, 1, 1, 1, 1};
		double[] xs = BSplineTestData.randomCoordinates(random, 10, 0, 10);
		double[] ys = BSplineTestData.randomCoordinates(random, 10, 0, 10);
		BSplineData data = BSplineTestData.data(3, knots, xs, ys, null);
		for (BSplineInteriorKnot knot : data.interiorKnots()) {
			double u = knot.value();
			int right = data.findSpan(u).spanKnotIndex();
			int left = data.findSpan(Math.nextDown(u)).spanKnotIndex();
			Fraction exactU = Fraction.of(u);
			Fraction[] leftPoint = ExactRationalBSplineReference.point(knots, 3, xs, ys, null,
					left, exactU);
			Fraction[] rightPoint = ExactRationalBSplineReference.point(knots, 3, xs, ys, null,
					right, exactU);
			Fraction[] leftDerivative = ExactRationalBSplineReference.derivative(knots, 3, xs, ys,
					null, left, exactU);
			Fraction[] rightDerivative = ExactRationalBSplineReference.derivative(knots, 3, xs,
					ys, null, right, exactU);
			// Exact reference: C^0 always, C^1 exactly when p - k >= 1.
			assertEquals(leftPoint[0], rightPoint[0]);
			assertEquals(leftPoint[1], rightPoint[1]);
			boolean c1 = knot.continuityOrder() >= 1;
			assertEquals(c1, leftDerivative[0].equals(rightDerivative[0])
					&& leftDerivative[1].equals(rightDerivative[1]));
			BSplineErrorCriteria.checkPolynomial(data, left, u,
					BSplineEvaluation.pointInSpan(data, left, u),
					BSplineEvaluation.firstDerivativeInSpan(data, left, u), ratios);
			BSplineErrorCriteria.checkPolynomial(data, right, u, BSplineEvaluation.point(data, u),
					BSplineEvaluation.firstDerivative(data, u), ratios);
		}
		assertEquals(2, data.interiorKnots().get(1).multiplicity());
		assertEquals(1, data.interiorKnots().get(1).continuityOrder());
		assertEquals(0, data.interiorKnots().get(2).continuityOrder());
		System.out.print(ratios.report(false));
	}

	@Test
	void degenerateControlPolygons() {
		double[] knots = {0, 0, 0, 0, 0.5, 1, 1, 1, 1};
		double[] same = {3.3, 3.3, 3.3, 3.3, 3.3};
		double[] sameY = {-0.7, -0.7, -0.7, -0.7, -0.7};
		BSplineData point = BSplineTestData.data(3, knots, same, sameY, null);
		BSplineErrorCriteria.Ratios ratios = new BSplineErrorCriteria.Ratios("degenerate");
		for (double u = 0.0; u <= 1.0; u += 1.0 / 64) {
			BSplinePoint value = BSplineEvaluation.point(point, u);
			BSplinePoint derivative = BSplineEvaluation.firstDerivative(point, u);
			assertEquals(0.0, derivative.x());
			assertEquals(0.0, derivative.y());
			BSplineErrorCriteria.checkPolynomial(point, value.spanKnotIndex(), u, value,
					derivative, ratios);
		}
		// Consecutive coincident points: stationary at u = a (E5: no relative assertion).
		BSplineData cusp = BSplineTestData.data(3, new double[] {0, 0, 0, 0, 1, 1, 1, 1},
				new double[] {0, 0, 1, 2}, new double[] {0, 0, 1, 0}, null);
		BSplinePoint start = BSplineEvaluation.firstDerivative(cusp, 0.0);
		assertEquals(0.0, start.x());
		assertEquals(0.0, start.y());
		for (double u = 0.0; u <= 1.0; u += 1.0 / 64) {
			BSplineErrorCriteria.checkPolynomial(cusp, 3, u, BSplineEvaluation.point(cusp, u),
					BSplineEvaluation.firstDerivative(cusp, u), ratios);
		}
		System.out.print(ratios.report(false));
	}

	@Test
	void secondDerivativeThroughRepeatedHodograph() {
		// Cubic Bezier: C'' (u) = 6 sum (P_{i+2} - 2 P_{i+1} + P_i) B_{i,1}(u).
		double[] xs = {0, 1, 3, 4};
		double[] ys = {0, 2, -1, 1};
		BSplineData data = BSplineTestData.data(3, new double[] {0, 0, 0, 0, 1, 1, 1, 1}, xs,
				ys, null);
		BSplineData second = data.hodograph().value().hodograph().value();
		for (double u : new double[] {0, 0.25, 0.5, 1}) {
			double ex = 6 * ((xs[2] - 2 * xs[1] + xs[0]) * (1 - u) + (xs[3] - 2 * xs[2] + xs[1])
					* u);
			double ey = 6 * ((ys[2] - 2 * ys[1] + ys[0]) * (1 - u) + (ys[3] - 2 * ys[2] + ys[1])
					* u);
			BSplinePoint value = BSplineEvaluation.point(second, u);
			assertEquals(ex, value.x(), 1e-14);
			assertEquals(ey, value.y(), 1e-14);
		}
	}

	@Test
	void evaluationIsReproducibleAndStateless() {
		Random random = new Random(BSplineTestData.SEED + 11);
		double[] knots = BSplineTestData.randomKnots(random, 5, 20, 0, 1);
		BSplineData data = BSplineTestData.data(5, knots,
				BSplineTestData.randomCoordinates(random, 20, 0, 1),
				BSplineTestData.randomCoordinates(random, 20, 0, 1), null);
		List<Double> parameters = BSplineTestData.parameters(data, random, 64);
		long[] first = new long[2 * parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			first[2 * i] = bits(BSplineEvaluation.point(data, parameters.get(i)).x());
			first[2 * i + 1] = bits(BSplineEvaluation.firstDerivative(data, parameters.get(i))
					.y());
		}
		for (int i = parameters.size() - 1; i >= 0; i--) {
			assertEquals(first[2 * i], bits(BSplineEvaluation.point(data, parameters.get(i)).x()));
			assertEquals(first[2 * i + 1], bits(BSplineEvaluation.firstDerivative(data,
					parameters.get(i)).y()));
		}
	}

	@Test
	void typedStatusesInsteadOfExceptions() {
		BSplineData data = BSplineTestData.data(2, new double[] {0, 0, 0, 1, 2, 2, 2},
				new double[] {0, 1, 2, 3}, new double[] {0, 1, 0, 1}, null);
		assertEquals(BSplineStatus.PARAMETER_NONFINITE,
				BSplineEvaluation.point(data, Double.NaN).status());
		assertEquals(BSplineStatus.PARAMETER_OUTSIDE_DOMAIN,
				BSplineEvaluation.point(data, 2.5).status());
		assertTrue(Double.isNaN(BSplineEvaluation.point(data, 2.5).x()));
		assertEquals(BSplineStatus.PARAMETER_OUTSIDE_DOMAIN,
				BSplineEvaluation.firstDerivative(data, -1).status());
		assertEquals(BSplineStatus.SPAN_NOT_NON_DEGENERATE,
				BSplineEvaluation.pointInSpan(data, 1, 0.5).status());
		assertEquals(BSplineStatus.SPAN_NOT_NON_DEGENERATE,
				BSplineEvaluation.firstDerivativeInSpan(data, 4, 0.5).status());
		assertEquals(BSplineStatus.PARAMETER_NONFINITE,
				BSplineEvaluation.pointInSpan(data, 2, Double.POSITIVE_INFINITY).status());
		assertTrue(BSplineEvaluation.pointInSpan(data, 2, 1.0).isOk());
	}
}

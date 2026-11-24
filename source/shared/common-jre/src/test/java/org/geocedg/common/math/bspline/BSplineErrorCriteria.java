/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;

/**
 * Error criteria E1 to E5 of the A-N1 validation matrix section 0.1 with the constants
 * derived for this library by the forward-error procedure of section 0.2 (derivation in the
 * PRE-G9B-MAINT-A-DXF0 library note). An observation above a derived constant fails the test:
 * the analysis or the implementation is wrong.
 *
 * <p>Polynomial (implicit unit weights): {@code K_pos(p) = 8p + 1},
 * {@code K_der(p) = 8p - 3}. Rational (explicit weights, homogeneous scales):
 * {@code K_pos^rat(p) = 8p + 3}, {@code K_der^rat(p) = 8p + 4}. E4 is asserted only where
 * {@code |C'| >= rho_der(p) S_der} with {@code rho_der(p) = K_der(p) 2^-33}, so that the
 * implied relative derivative bound is at most {@code 2^-20}. Span forms:
 * {@code K_bern(p) = 16p - 15}, {@code K_loc(p) = (17p - 14) 3^p + 1}, against the active
 * control-point scale.</p>
 */
final class BSplineErrorCriteria {

	/** Unit roundoff {@code 2^-53}. */
	static final double EPS = 0x1p-53;

	private BSplineErrorCriteria() {
	}

	static double kPos(int p) {
		return 8.0 * p + 1.0;
	}

	static double kDer(int p) {
		return 8.0 * p - 3.0;
	}

	static double kPosRational(int p) {
		return 8.0 * p + 3.0;
	}

	static double kDerRational(int p) {
		return 8.0 * p + 4.0;
	}

	static double rhoDer(int p) {
		return kDer(p) * 0x1p-33;
	}

	static double rhoDerRational(int p) {
		return kDerRational(p) * 0x1p-33;
	}

	static double kBernstein(int p) {
		return 16.0 * p - 15.0;
	}

	static double kLocal(int p) {
		return (17.0 * p - 14.0) * Math.pow(3.0, p) + 1.0;
	}

	/** Running maxima of observed ratios {@code error / (eps * S)} per degree. */
	static final class Ratios {
		private final String label;
		final double[] position = new double[32];
		final double[] derivative = new double[32];
		final double[] relativeDerivative = new double[32];
		final int[] samples = new int[32];

		Ratios(String label) {
			this.label = label;
		}

		void position(int p, double ratio) {
			position[p] = Math.max(position[p], ratio);
			samples[p]++;
		}

		void derivative(int p, double ratio) {
			derivative[p] = Math.max(derivative[p], ratio);
		}

		void relativeDerivative(int p, double ratio) {
			relativeDerivative[p] = Math.max(relativeDerivative[p], ratio);
		}

		String report(boolean rational) {
			StringBuilder text = new StringBuilder();
			for (int p = 0; p < samples.length; p++) {
				if (samples[p] == 0) {
					continue;
				}
				double kp = rational ? kPosRational(p) : kPos(p);
				double kd = rational ? kDerRational(p) : kDer(p);
				text.append(String.format(Locale.ROOT,
						"BSPLINE-RATIOS %s p=%d samples=%d maxE1=%.4f K_pos=%.0f maxE3=%.4f"
								+ " K_der=%.0f maxE4rel=%.3e%n",
						label, p, samples[p], position[p], kp, derivative[p], kd,
						relativeDerivative[p]));
			}
			return text.toString();
		}
	}

	/**
	 * Checks E1/E3/E4/E5 for a polynomial evaluation against the exact reference and records
	 * the ratios.
	 */
	static void checkPolynomial(BSplineData data, int span, double u, BSplinePoint point,
			BSplinePoint derivative, Ratios ratios) {
		int p = data.degree();
		double[] knots = data.knots();
		double[] xs = data.xs();
		double[] ys = data.ys();
		Fraction exactU = Fraction.of(u);
		Fraction[] c = ExactRationalBSplineReference.point(knots, p, xs, ys, null, span, exactU);
		double error = ExactRationalBSplineReference.norm(Fraction.of(point.x()).sub(c[0]),
				Fraction.of(point.y()).sub(c[1]));
		double sPos = 0.0;
		for (int i = span - p; i <= span; i++) {
			sPos = Math.max(sPos, Math.hypot(xs[i], ys[i]));
		}
		double e1 = ratio(error, sPos);
		ratios.position(p, e1);
		assertTrue(e1 <= kPos(p), () -> String.format(Locale.ROOT,
				"E1 exceeded: p=%d u=%s ratio=%s K=%s", p, u, e1, kPos(p)));
		Fraction[] d = ExactRationalBSplineReference.derivative(knots, p, xs, ys, null, span,
				exactU);
		double derivativeError = ExactRationalBSplineReference.norm(
				Fraction.of(derivative.x()).sub(d[0]), Fraction.of(derivative.y()).sub(d[1]));
		double sDer = 0.0;
		for (int i = span - p; i <= span - 1; i++) {
			double width = knots[i + p + 1] - knots[i + 1];
			sDer = Math.max(sDer, p * (Math.hypot(xs[i + 1], ys[i + 1])
					+ Math.hypot(xs[i], ys[i])) / width);
		}
		double e3 = ratio(derivativeError, sDer);
		ratios.derivative(p, e3);
		assertTrue(e3 <= kDer(p), () -> String.format(Locale.ROOT,
				"E3 exceeded: p=%d u=%s ratio=%s K=%s", p, u, e3, kDer(p)));
		double exactNorm = ExactRationalBSplineReference.norm(d[0], d[1]);
		if (exactNorm >= rhoDer(p) * sDer && exactNorm > 0.0) {
			// E4: relative error reported with the conditioning factor S_der / |C'|.
			double relative = derivativeError / exactNorm;
			ratios.relativeDerivative(p, relative);
			assertTrue(relative <= kDer(p) * EPS * sDer / exactNorm);
		}
	}

	/**
	 * Checks the rational criteria with the homogeneous scales of the derivation note and
	 * records the ratios.
	 */
	static void checkRational(BSplineData data, int span, double u, BSplinePoint point,
			BSplinePoint derivative, Ratios ratios) {
		int p = data.degree();
		double[] knots = data.knots();
		double[] xs = data.xs();
		double[] ys = data.ys();
		double[] ws = data.explicitWeights();
		Fraction exactU = Fraction.of(u);
		Fraction[] h = ExactRationalBSplineReference.homogeneous(knots, p, xs, ys, ws, span,
				exactU);
		Fraction[] hd = ExactRationalBSplineReference.homogeneousDerivative(knots, p, xs, ys, ws,
				span, exactU);
		Fraction cx = h[0].div(h[2]);
		Fraction cy = h[1].div(h[2]);
		Fraction w2 = h[2].mul(h[2]);
		Fraction dx = hd[0].mul(h[2]).sub(h[0].mul(hd[2])).div(w2);
		Fraction dy = hd[1].mul(h[2]).sub(h[1].mul(hd[2])).div(w2);
		double w = h[2].approx();
		double dw = Math.abs(hd[2].approx());
		double cNorm = ExactRationalBSplineReference.norm(cx, cy);
		double dNorm = ExactRationalBSplineReference.norm(dx, dy);
		double maxW = 0.0;
		double maxWP = 0.0;
		for (int i = span - p; i <= span; i++) {
			maxW = Math.max(maxW, ws[i]);
			maxWP = Math.max(maxWP, ws[i] * Math.hypot(xs[i], ys[i]));
		}
		double sPos = (maxWP + cNorm * maxW) / w;
		double error = ExactRationalBSplineReference.norm(Fraction.of(point.x()).sub(cx),
				Fraction.of(point.y()).sub(cy));
		double e1 = ratio(error, sPos);
		ratios.position(p, e1);
		assertTrue(e1 <= kPosRational(p), () -> String.format(Locale.ROOT,
				"rational E1 exceeded: p=%d u=%s ratio=%s", p, u, e1));
		double sA = 0.0;
		double sW = 0.0;
		for (int i = span - p; i <= span - 1; i++) {
			double width = knots[i + p + 1] - knots[i + 1];
			sA = Math.max(sA, p * (ws[i + 1] * Math.hypot(xs[i + 1], ys[i + 1])
					+ ws[i] * Math.hypot(xs[i], ys[i])) / width);
			sW = Math.max(sW, p * (ws[i + 1] + ws[i]) / width);
		}
		double sDer = (sA + cNorm * sW + dw * sPos + dNorm * maxW) / w;
		double derivativeError = ExactRationalBSplineReference.norm(
				Fraction.of(derivative.x()).sub(dx), Fraction.of(derivative.y()).sub(dy));
		double e3 = ratio(derivativeError, sDer);
		ratios.derivative(p, e3);
		assertTrue(e3 <= kDerRational(p), () -> String.format(Locale.ROOT,
				"rational E3 exceeded: p=%d u=%s ratio=%s", p, u, e3));
		if (dNorm >= rhoDerRational(p) * sDer && dNorm > 0.0) {
			double relative = derivativeError / dNorm;
			ratios.relativeDerivative(p, relative);
			assertTrue(relative <= kDerRational(p) * EPS * sDer / dNorm);
		}
	}

	private static double ratio(double error, double scale) {
		if (scale == 0.0) {
			assertTrue(error == 0.0, "nonzero error at zero scale");
			return 0.0;
		}
		return error / (EPS * scale);
	}
}

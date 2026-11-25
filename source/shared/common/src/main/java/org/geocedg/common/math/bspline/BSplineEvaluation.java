/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import javax.annotation.CheckForNull;

/**
 * Point and first-derivative evaluation by the homogeneous de Boor algorithm
 * ({@code bspline-2d} section 11.2). Stateless; every method is total on validated data and
 * reports a typed status instead of throwing.
 *
 * <h2>Fixed operation sequence</h2>
 *
 * <p><b>de Boor, degree {@code q}, span {@code j}.</b> The working values are
 * {@code d_i = P_{j-q+i}}, {@code i = 0..q}. For {@code r = 1..q} and {@code i = q} down to
 * {@code r}:</p>
 * <pre>
 *   lo    = u[j - q + i]
 *   hi    = u[j + 1 + i - r]
 *   alpha = (u - lo) / (hi - lo)
 *   beta  = 1.0 - alpha
 *   d_i   = beta * d_{i-1} + alpha * d_i      (per coordinate, left to right)
 * </pre>
 * <p>and the result is {@code d_q}. Java {@code double} arithmetic is IEEE 754 binary64 with
 * round-to-nearest-even; no fused multiply-add is used. The convex form makes the clamped end
 * values exact ({@code alpha = 0} at {@code u = a} and {@code alpha = 1} at {@code u = b}).</p>
 *
 * <p><b>Polynomial data</b> (implicit unit weights): the coordinates are {@code x} and
 * {@code y} only; no weight is materialized, nothing is multiplied by 1 and nothing is
 * divided by {@code W}, so the result is bit-identical to the polynomial de Boor algorithm
 * (BS-15). <b>Rational data</b> (explicit weights): the coordinates are the homogeneous
 * values {@code (w_i * x_i, w_i * y_i, w_i)}; then {@code x = A_x / W}, {@code y = A_y / W}.
 * Explicit weights that are all equal are still evaluated by the rational sequence; use
 * {@link BSplineData#polynomialEquivalent()} to evaluate the polynomial sequence.</p>
 *
 * <p><b>First derivative, polynomial.</b> The hodograph coefficients
 * {@code Q_k = p * ((c_{k+1} - c_k) / (u[k+p+1] - u[k+1]))} for {@code k = j-p..j-1}, then
 * de Boor of degree {@code p - 1} on the inner knots (knot index shifted by one, span
 * {@code j - 1}). <b>Rational.</b> The same hodograph sequence on the homogeneous values gives
 * {@code A'} and {@code W'}; with {@code A} and {@code W} from the point sequence,
 * {@code c = A_c / W} and {@code c' = (A'_c - W' * c) / W} per coordinate.</p>
 */
public final class BSplineEvaluation {

	private BSplineEvaluation() {
	}

	/**
	 * Evaluates the curve at a parameter of the closed domain, in its owning span.
	 *
	 * @param data validated data
	 * @param u parameter
	 * @return the point, or {@link BSplineStatus#PARAMETER_NONFINITE} /
	 *         {@link BSplineStatus#PARAMETER_OUTSIDE_DOMAIN}
	 */
	public static BSplinePoint point(BSplineData data, double u) {
		BSplineSpanLookup span = data.findSpan(u);
		if (!span.isOk()) {
			return BSplinePoint.failure(span.status());
		}
		return pointOnSpan(data, span.spanKnotIndex(), u);
	}

	/**
	 * Evaluates the polynomial (or rational) piece of one non-degenerate span at {@code u}.
	 * The parameter may lie anywhere; at the end knot {@code u_{j+1}} this gives the one-sided
	 * limit from the left of the next span.
	 *
	 * @param data validated data
	 * @param spanKnotIndex knot index {@code j} of a non-degenerate span
	 * @param u finite parameter
	 * @return the point, or {@link BSplineStatus#SPAN_NOT_NON_DEGENERATE} /
	 *         {@link BSplineStatus#PARAMETER_NONFINITE}
	 */
	public static BSplinePoint pointInSpan(BSplineData data, int spanKnotIndex, double u) {
		if (!Double.isFinite(u)) {
			return BSplinePoint.failure(BSplineStatus.PARAMETER_NONFINITE);
		}
		if (!data.isSpanKnotIndex(spanKnotIndex)) {
			return BSplinePoint.failure(BSplineStatus.SPAN_NOT_NON_DEGENERATE);
		}
		return pointOnSpan(data, spanKnotIndex, u);
	}

	/**
	 * Evaluates the first derivative with respect to {@code u} at a parameter of the closed
	 * domain, in its owning span (the right-hand derivative at an interior knot).
	 *
	 * @param data validated data
	 * @param u parameter
	 * @return the derivative vector, or {@link BSplineStatus#PARAMETER_NONFINITE} /
	 *         {@link BSplineStatus#PARAMETER_OUTSIDE_DOMAIN}
	 */
	public static BSplinePoint firstDerivative(BSplineData data, double u) {
		BSplineSpanLookup span = data.findSpan(u);
		if (!span.isOk()) {
			return BSplinePoint.failure(span.status());
		}
		return derivativeOnSpan(data, span.spanKnotIndex(), u);
	}

	/**
	 * Evaluates the first derivative of the piece of one non-degenerate span at {@code u}
	 * (at {@code u_{j+1}}: the left-hand derivative of the next knot).
	 *
	 * @param data validated data
	 * @param spanKnotIndex knot index {@code j} of a non-degenerate span
	 * @param u finite parameter
	 * @return the derivative vector, or {@link BSplineStatus#SPAN_NOT_NON_DEGENERATE} /
	 *         {@link BSplineStatus#PARAMETER_NONFINITE}
	 */
	public static BSplinePoint firstDerivativeInSpan(BSplineData data, int spanKnotIndex,
			double u) {
		if (!Double.isFinite(u)) {
			return BSplinePoint.failure(BSplineStatus.PARAMETER_NONFINITE);
		}
		if (!data.isSpanKnotIndex(spanKnotIndex)) {
			return BSplinePoint.failure(BSplineStatus.SPAN_NOT_NON_DEGENERATE);
		}
		return derivativeOnSpan(data, spanKnotIndex, u);
	}

	private static BSplinePoint pointOnSpan(BSplineData data, int j, double u) {
		int p = data.degree;
		double[] cx = new double[p + 1];
		double[] cy = new double[p + 1];
		double[] ws = data.ws;
		if (ws == null) {
			for (int i = 0; i <= p; i++) {
				cx[i] = data.xs[j - p + i];
				cy[i] = data.ys[j - p + i];
			}
			deBoor(p, data.knots, 0, j, u, cx, cy, null);
			return BSplinePoint.of(cx[p], cy[p], j);
		}
		double[] cw = new double[p + 1];
		loadHomogeneous(data, j, cx, cy, cw);
		deBoor(p, data.knots, 0, j, u, cx, cy, cw);
		return BSplinePoint.of(cx[p] / cw[p], cy[p] / cw[p], j);
	}

	private static BSplinePoint derivativeOnSpan(BSplineData data, int j, double u) {
		int p = data.degree;
		if (p == 0) {
			return BSplinePoint.of(0.0, 0.0, j);
		}
		double[] ws = data.ws;
		double[] qx = new double[p];
		double[] qy = new double[p];
		if (ws == null) {
			for (int i = 0; i < p; i++) {
				qx[i] = hodographCoefficient(p, data.knots, data.xs, j - p + i);
				qy[i] = hodographCoefficient(p, data.knots, data.ys, j - p + i);
			}
			deBoor(p - 1, data.knots, 1, j - 1, u, qx, qy, null);
			return BSplinePoint.of(qx[p - 1], qy[p - 1], j);
		}
		double[] hx = new double[p + 1];
		double[] hy = new double[p + 1];
		double[] hw = new double[p + 1];
		loadHomogeneous(data, j, hx, hy, hw);
		double[] qw = new double[p];
		for (int i = 0; i < p; i++) {
			int k = j - p + i;
			double span = data.knots[k + p + 1] - data.knots[k + 1];
			qx[i] = p * ((hx[i + 1] - hx[i]) / span);
			qy[i] = p * ((hy[i + 1] - hy[i]) / span);
			qw[i] = p * ((hw[i + 1] - hw[i]) / span);
		}
		deBoor(p - 1, data.knots, 1, j - 1, u, qx, qy, qw);
		deBoor(p, data.knots, 0, j, u, hx, hy, hw);
		double w = hw[p];
		double cx = hx[p] / w;
		double cy = hy[p] / w;
		double dw = qw[p - 1];
		return BSplinePoint.of((qx[p - 1] - dw * cx) / w, (qy[p - 1] - dw * cy) / w, j);
	}

	private static void loadHomogeneous(BSplineData data, int j, double[] cx, double[] cy,
			double[] cw) {
		int p = data.degree;
		for (int i = 0; i <= p; i++) {
			int k = j - p + i;
			double w = data.ws[k];
			cx[i] = w * data.xs[k];
			cy[i] = w * data.ys[k];
			cw[i] = w;
		}
	}

	/**
	 * Hodograph coefficient {@code Q_k = p * ((c_{k+1} - c_k) / (u[k+p+1] - u[k+1]))}; a zero
	 * denominator (empty support) gives 0.
	 */
	static double hodographCoefficient(int p, double[] knots, double[] c, int k) {
		double span = knots[k + p + 1] - knots[k + 1];
		if (span == 0.0) {
			return 0.0;
		}
		return p * ((c[k + 1] - c[k]) / span);
	}

	/**
	 * In-place de Boor of degree {@code q}; knot index {@code t} reads
	 * {@code knots[t + offset]}; {@code j} is the span index in that indexing; the arrays hold
	 * {@code d_0..d_q} on entry and {@code d_q} is the result.
	 */
	static void deBoor(int q, double[] knots, int offset, int j, double u, double[] c0,
			double[] c1, @CheckForNull double[] c2) {
		for (int r = 1; r <= q; r++) {
			for (int i = q; i >= r; i--) {
				double lo = knots[j - q + i + offset];
				double hi = knots[j + 1 + i - r + offset];
				double alpha = (u - lo) / (hi - lo);
				double beta = 1.0 - alpha;
				c0[i] = beta * c0[i - 1] + alpha * c0[i];
				c1[i] = beta * c1[i - 1] + alpha * c1[i];
				if (c2 != null) {
					c2[i] = beta * c2[i - 1] + alpha * c2[i];
				}
			}
		}
	}
}

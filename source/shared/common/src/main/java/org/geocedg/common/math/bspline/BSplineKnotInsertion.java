/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Boehm knot insertion: exact in exact arithmetic, rounded in binary64.
 *
 * <h2>Fixed operation sequence</h2>
 *
 * <p>To insert {@code t} once, let {@code k} be the owning span of {@code t} (right-owned,
 * so {@code u_k <= t < u_{k+1}}) and {@code s} the multiplicity of {@code t}. The new knot
 * vector has {@code t} inserted after index {@code k}. The new control points are, for
 * {@code i = 0..n+1}:</p>
 * <pre>
 *   i &lt;= k - p              : Q_i = P_i                           (copy)
 *   i &gt;  k - s              : Q_i = P_{i-1}                       (copy)
 *   otherwise               : alpha = (t - u[i]) / (u[i + p] - u[i])
 *                             beta  = 1.0 - alpha
 *                             Q_i   = beta * P_{i-1} + alpha * P_i  (per coordinate)
 * </pre>
 * <p>For implicit unit weights the coordinates are {@code x, y}. For explicit weights the
 * combination acts on the homogeneous values {@code (w x, w y, w)} and the new point is
 * projected back as {@code x = (w x) / w}, {@code y = (w y) / w} with weight {@code w};
 * copied points keep their stored values exactly. The span Bernstein form of
 * {@link BSplineSpanForms} applies the same per-point sequence to a local window, so the two
 * are bit-identical for polynomial data.</p>
 */
public final class BSplineKnotInsertion {

	private BSplineKnotInsertion() {
	}

	/**
	 * Inserts one knot value strictly inside the domain.
	 *
	 * @param data validated data
	 * @param t knot value, {@code a < t < b}
	 * @return new immutable data with one more knot and one more control point, or
	 *         {@link BSplineStatus#PARAMETER_NONFINITE},
	 *         {@link BSplineStatus#KNOT_NOT_INTERIOR},
	 *         {@link BSplineStatus#MULTIPLICITY_LIMIT} (the result would have multiplicity
	 *         above the degree) or {@link BSplineStatus#SIZE_LIMIT}
	 */
	public static BSplineOutcome<BSplineData> insert(BSplineData data, double t) {
		if (!Double.isFinite(t)) {
			return BSplineOutcome.failure(BSplineStatus.PARAMETER_NONFINITE);
		}
		if (!(data.domainStart() < t && t < data.domainEnd())) {
			return BSplineOutcome.failure(BSplineStatus.KNOT_NOT_INTERIOR);
		}
		int p = data.degree;
		int s = data.multiplicity(t);
		if (s + 1 > p) {
			return BSplineOutcome.failure(BSplineStatus.MULTIPLICITY_LIMIT);
		}
		int n = data.xs.length - 1;
		if (n + 2 > BSplineLimits.ABSOLUTE_MAX_CONTROL_POINTS) {
			return BSplineOutcome.failure(BSplineStatus.SIZE_LIMIT);
		}
		int k = data.findSpan(t).spanKnotIndex();
		double[] u = data.knots;
		double[] knots = new double[u.length + 1];
		System.arraycopy(u, 0, knots, 0, k + 1);
		knots[k + 1] = t;
		System.arraycopy(u, k + 1, knots, k + 2, u.length - k - 1);
		double[] x = new double[n + 2];
		double[] y = new double[n + 2];
		double[] ws = data.ws;
		double[] w = ws == null ? null : new double[n + 2];
		for (int i = 0; i <= n + 1; i++) {
			int source = i <= k - p ? i : i > k - s ? i - 1 : -1;
			if (source >= 0) {
				x[i] = data.xs[source];
				y[i] = data.ys[source];
				if (w != null) {
					w[i] = ws[source];
				}
				continue;
			}
			double alpha = (t - u[i]) / (u[i + p] - u[i]);
			double beta = 1.0 - alpha;
			if (ws == null) {
				x[i] = beta * data.xs[i - 1] + alpha * data.xs[i];
				y[i] = beta * data.ys[i - 1] + alpha * data.ys[i];
			} else {
				double hx = beta * (ws[i - 1] * data.xs[i - 1]) + alpha * (ws[i] * data.xs[i]);
				double hy = beta * (ws[i - 1] * data.ys[i - 1]) + alpha * (ws[i] * data.ys[i]);
				double hw = beta * ws[i - 1] + alpha * ws[i];
				x[i] = hx / hw;
				y[i] = hy / hw;
				w[i] = hw;
			}
		}
		return BSplineOutcome.ok(BSplineData.trusted(p, knots, x, y, w));
	}
}

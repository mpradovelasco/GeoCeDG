/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Per-span polynomial forms ({@code bspline-2d} section 11.3).
 *
 * <h2>Bernstein form</h2>
 *
 * <p>Knot insertion to multiplicity {@code p} at both span ends, on a local window of the
 * {@code p + 1} active control points {@code P_{j-p}..P_j} (homogeneous {@code (w x, w y, w)}
 * when the weights are explicit, never projected) and the {@code 2p + 2} knots
 * {@code u_{j-p}..u_{j+p+1}}. First {@code u_j} is inserted until its multiplicity is
 * {@code p}, then {@code u_{j+1}}. Each insertion computes each new point by the per-point
 * sequence of {@link BSplineKnotInsertion}, so for polynomial data the coefficients are
 * bit-identical to those obtained by inserting the same values, in the same order, into the
 * whole data. The resulting {@code p + 1} points are the Bernstein coefficients of the span
 * in {@code s}.</p>
 *
 * <p><b>Locality.</b> The computation of span {@code j} reads only the {@code p + 1} active
 * control points (and weights) and the knots {@code u_{j-p}..u_{j+p+1}} of that span (the two
 * outer knots only to count end multiplicities); it never refines the whole knot vector. So
 * changing one control point {@code P_k} changes, bit for bit, only the forms of the spans
 * {@code j} with {@code j - p <= k <= j}, and the result of a span never depends on other
 * spans or on any insertion order outside its window.</p>
 *
 * <h2>Local power form</h2>
 *
 * <p>From the Bernstein coefficients {@code b_0..b_p}, per coordinate and for each
 * {@code k = 0..p}:</p>
 * <pre>
 *   sum = (k odd ? -1 : 1) * (C(k,0) * b_0)
 *   for i = 1..k:  term = C(k,i) * b_i;  sum = ((k - i) odd) ? sum - term : sum + term
 *   a_k = C(p,k) * sum
 * </pre>
 * <p>with exact binary64 binomials. The transform amplifies coefficient errors by at most
 * {@code max_k C(p,k) 2^k <= 3^p}, independently of the knot values.</p>
 *
 * <h2>Global power coefficients</h2>
 *
 * <p>For polynomial data only: {@code c_m = sum_{k>=m} a_k C(k,m) (-u_j)^{k-m} / h^k} with
 * {@code h = u_{j+1} - u_j}, all in exact rational arithmetic on the binary64 values, each
 * rounded once (round-half-even); see {@link BSplineGlobalPolynomial}.</p>
 */
public final class BSplineSpanForms {

	private BSplineSpanForms() {
	}

	/**
	 * Bernstein coefficients of one span.
	 *
	 * @param data validated data
	 * @param spanKnotIndex knot index {@code j} of a non-degenerate span
	 * @return the coefficients, or {@link BSplineStatus#SPAN_NOT_NON_DEGENERATE}
	 */
	public static BSplineOutcome<BSplineSpanPolynomial> bernstein(BSplineData data,
			int spanKnotIndex) {
		if (!data.isSpanKnotIndex(spanKnotIndex)) {
			return BSplineOutcome.failure(BSplineStatus.SPAN_NOT_NON_DEGENERATE);
		}
		int p = data.degree;
		int j = spanKnotIndex;
		double[] window = new double[2 * p + 2];
		System.arraycopy(data.knots, j - p, window, 0, window.length);
		double[] cx = new double[p + 1];
		double[] cy = new double[p + 1];
		double[] cw = null;
		if (data.ws == null) {
			System.arraycopy(data.xs, j - p, cx, 0, p + 1);
			System.arraycopy(data.ys, j - p, cy, 0, p + 1);
		} else {
			cw = new double[p + 1];
			for (int i = 0; i <= p; i++) {
				int k = j - p + i;
				double weight = data.ws[k];
				cx[i] = weight * data.xs[k];
				cy[i] = weight * data.ys[k];
				cw[i] = weight;
			}
		}
		double start = window[p];
		double end = window[p + 1];
		while (count(window, start) < p) {
			window = insertAtStart(p, window, cx, cy, cw);
		}
		while (count(window, end) < p) {
			window = insertAtEnd(p, window, cx, cy, cw);
		}
		return BSplineOutcome.ok(new BSplineSpanPolynomial(BSplineSpanPolynomial.Form.BERNSTEIN,
				p, j, start, end, cx, cy, cw));
	}

	/**
	 * Local power coefficients of one span in {@code s}, by the fixed binomial transform of
	 * the Bernstein coefficients.
	 *
	 * @param data validated data
	 * @param spanKnotIndex knot index {@code j} of a non-degenerate span
	 * @return the coefficients, or {@link BSplineStatus#SPAN_NOT_NON_DEGENERATE}
	 */
	public static BSplineOutcome<BSplineSpanPolynomial> localPower(BSplineData data,
			int spanKnotIndex) {
		BSplineOutcome<BSplineSpanPolynomial> bernstein = bernstein(data, spanKnotIndex);
		if (!bernstein.isOk()) {
			return bernstein;
		}
		BSplineSpanPolynomial b = bernstein.value();
		int p = b.degree();
		double[][] binomial = binomials(p);
		double[] bw = b.w();
		return BSplineOutcome.ok(new BSplineSpanPolynomial(
				BSplineSpanPolynomial.Form.LOCAL_POWER, p, b.spanKnotIndex(), b.spanStart(),
				b.spanEnd(), toPower(binomial, b.x()), toPower(binomial, b.y()),
				bw == null ? null : toPower(binomial, bw)));
	}

	/**
	 * The local power coefficients of {@link #localPower} in descending powers of {@code s}
	 * (index {@code i} holds the coefficient of {@code s^(p-i)}; the constant term is last).
	 * The values are bit-identical to those of {@link #localPower}, reversed.
	 *
	 * @param data validated data
	 * @param spanKnotIndex knot index {@code j} of a non-degenerate span
	 * @return the coefficients, or {@link BSplineStatus#SPAN_NOT_NON_DEGENERATE}
	 */
	public static BSplineOutcome<BSplineSpanPolynomial> localPowerDescending(BSplineData data,
			int spanKnotIndex) {
		BSplineOutcome<BSplineSpanPolynomial> ascending = localPower(data, spanKnotIndex);
		if (!ascending.isOk()) {
			return ascending;
		}
		BSplineSpanPolynomial a = ascending.value();
		double[] aw = a.w();
		return BSplineOutcome.ok(new BSplineSpanPolynomial(
				BSplineSpanPolynomial.Form.LOCAL_POWER_DESCENDING, a.degree(), a.spanKnotIndex(),
				a.spanStart(), a.spanEnd(), reversed(a.x()), reversed(a.y()),
				aw == null ? null : reversed(aw)));
	}

	private static double[] reversed(double[] values) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[values.length - 1 - i];
		}
		return result;
	}

	/**
	 * Global power coefficients of one span of polynomial data.
	 *
	 * @param data validated data with implicit unit weights
	 * @param spanKnotIndex knot index {@code j} of a non-degenerate span
	 * @return the coefficients, or {@link BSplineStatus#SPAN_NOT_NON_DEGENERATE} /
	 *         {@link BSplineStatus#EXPLICIT_WEIGHTS_UNSUPPORTED}
	 */
	public static BSplineOutcome<BSplineGlobalPolynomial> globalPower(BSplineData data,
			int spanKnotIndex) {
		if (data.ws != null) {
			return BSplineOutcome.failure(BSplineStatus.EXPLICIT_WEIGHTS_UNSUPPORTED);
		}
		BSplineOutcome<BSplineSpanPolynomial> local = localPower(data, spanKnotIndex);
		if (!local.isOk()) {
			return BSplineOutcome.failure(local.status());
		}
		BSplineSpanPolynomial a = local.value();
		int p = a.degree();
		BSplineExactRational start = BSplineExactRational.ofDouble(a.spanStart());
		BSplineExactRational width = BSplineExactRational.ofDouble(a.spanEnd()).subtract(start);
		BSplineExactRational inverseWidth = BSplineExactRational.ONE.divide(width);
		BSplineExactRational negativeStart = start.negate();
		double[][] binomial = binomials(p);
		return BSplineOutcome.ok(new BSplineGlobalPolynomial(p, a.spanKnotIndex(),
				toGlobal(p, a.x(), inverseWidth, negativeStart, binomial),
				toGlobal(p, a.y(), inverseWidth, negativeStart, binomial)));
	}

	private static double[] toGlobal(int p, double[] local, BSplineExactRational inverseWidth,
			BSplineExactRational negativeStart, double[][] binomial) {
		BSplineExactRational[] sums = new BSplineExactRational[p + 1];
		for (int m = 0; m <= p; m++) {
			sums[m] = BSplineExactRational.ZERO;
		}
		BSplineExactRational scale = BSplineExactRational.ONE;
		for (int k = 0; k <= p; k++) {
			BSplineExactRational base = BSplineExactRational.ofDouble(local[k]).multiply(scale);
			BSplineExactRational power = BSplineExactRational.ONE;
			for (int m = k; m >= 0; m--) {
				BSplineExactRational binomialValue = BSplineExactRational.ofLong(
						(long) binomial[k][m]);
				sums[m] = sums[m].add(base.multiply(binomialValue).multiply(power));
				power = power.multiply(negativeStart);
			}
			scale = scale.multiply(inverseWidth);
		}
		double[] result = new double[p + 1];
		for (int m = 0; m <= p; m++) {
			result[m] = sums[m].roundHalfEven();
		}
		return result;
	}

	private static double[] toPower(double[][] binomial, double[] b) {
		int p = b.length - 1;
		double[] a = new double[p + 1];
		for (int k = 0; k <= p; k++) {
			double sum = binomial[k][0] * b[0];
			if (k % 2 == 1) {
				sum = -sum;
			}
			for (int i = 1; i <= k; i++) {
				double term = binomial[k][i] * b[i];
				sum = (k - i) % 2 == 1 ? sum - term : sum + term;
			}
			a[k] = binomial[p][k] * sum;
		}
		return a;
	}

	/** Exact binary64 binomials {@code C(k, i)}, {@code 0 <= i <= k <= p}, by Pascal. */
	static double[][] binomials(int p) {
		double[][] c = new double[p + 1][];
		for (int k = 0; k <= p; k++) {
			c[k] = new double[k + 1];
			c[k][0] = 1.0;
			c[k][k] = 1.0;
			for (int i = 1; i < k; i++) {
				c[k][i] = c[k - 1][i - 1] + c[k - 1][i];
			}
		}
		return c;
	}

	private static int count(double[] values, double value) {
		int count = 0;
		for (double v : values) {
			if (v == value) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Inserts the span start {@code t = L[p]} into the window; returns the shifted window
	 * (first knot and first point dropped). Copy rule: {@code i > p - s} copies; otherwise the
	 * convex combination with {@code alpha = (t - L[i]) / (L[i + p] - L[i])}.
	 */
	private static double[] insertAtStart(int p, double[] window, double[] cx, double[] cy,
			double[] cw) {
		double t = window[p];
		int s = count(window, t);
		double[] nx = new double[p + 1];
		double[] ny = new double[p + 1];
		double[] nw = cw == null ? null : new double[p + 1];
		for (int i = 1; i <= p + 1; i++) {
			if (i > p - s) {
				nx[i - 1] = cx[i - 1];
				ny[i - 1] = cy[i - 1];
				if (nw != null) {
					nw[i - 1] = cw[i - 1];
				}
			} else {
				combine(window, t, p, i, cx, cy, cw, nx, ny, nw, i - 1);
			}
		}
		copyBack(nx, ny, nw, cx, cy, cw);
		double[] next = new double[window.length];
		System.arraycopy(window, 1, next, 0, p);
		next[p] = t;
		System.arraycopy(window, p + 1, next, p + 1, p + 1);
		return next;
	}

	/**
	 * Inserts the span end {@code t = L[p+1]} into the window after its last copy at index
	 * {@code k = p + s}; returns the window with its last knot dropped. Copy rule:
	 * {@code i <= s} copies; otherwise the convex combination.
	 */
	private static double[] insertAtEnd(int p, double[] window, double[] cx, double[] cy,
			double[] cw) {
		double t = window[p + 1];
		int s = count(window, t);
		double[] nx = new double[p + 1];
		double[] ny = new double[p + 1];
		double[] nw = cw == null ? null : new double[p + 1];
		for (int i = 0; i <= p; i++) {
			if (i <= s) {
				nx[i] = cx[i];
				ny[i] = cy[i];
				if (nw != null) {
					nw[i] = cw[i];
				}
			} else {
				combine(window, t, p, i, cx, cy, cw, nx, ny, nw, i);
			}
		}
		copyBack(nx, ny, nw, cx, cy, cw);
		int k = p + s;
		double[] next = new double[window.length];
		System.arraycopy(window, 0, next, 0, k + 1);
		next[k + 1] = t;
		System.arraycopy(window, k + 1, next, k + 2, window.length - k - 2);
		return next;
	}

	private static void combine(double[] window, double t, int p, int i, double[] cx,
			double[] cy, double[] cw, double[] nx, double[] ny, double[] nw, int target) {
		double alpha = (t - window[i]) / (window[i + p] - window[i]);
		double beta = 1.0 - alpha;
		nx[target] = beta * cx[i - 1] + alpha * cx[i];
		ny[target] = beta * cy[i - 1] + alpha * cy[i];
		if (nw != null) {
			nw[target] = beta * cw[i - 1] + alpha * cw[i];
		}
	}

	private static void copyBack(double[] nx, double[] ny, double[] nw, double[] cx,
			double[] cy, double[] cw) {
		System.arraycopy(nx, 0, cx, 0, nx.length);
		System.arraycopy(ny, 0, cy, 0, ny.length);
		if (nw != null) {
			System.arraycopy(nw, 0, cw, 0, nw.length);
		}
	}
}

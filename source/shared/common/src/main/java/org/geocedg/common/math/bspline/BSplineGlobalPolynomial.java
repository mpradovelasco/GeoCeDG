/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Immutable global power coefficients of one span of polynomial data: the coordinate
 * functions are {@code sum_k c_k u^k}, {@code k = 0..p}, in the global parameter {@code u}.
 *
 * <p>Each coefficient is the correctly rounded (round-half-even) binary64 value of the exact
 * expansion, in rational arithmetic, of the span's local power coefficients (binary64 values)
 * with {@code s = (u - u_j) / (u_{j+1} - u_j)} exact ({@code bspline-2d} section 11.3 item 3).
 * They are a correctly rounded expansion of the published local polynomial, never the
 * polynomial authority. A coefficient whose exact value exceeds the binary64 range is an
 * infinity; {@link #allFinite()} reports it.</p>
 */
public final class BSplineGlobalPolynomial {

	private final int degree;
	private final int spanKnotIndex;
	private final double[] x;
	private final double[] y;

	BSplineGlobalPolynomial(int degree, int spanKnotIndex, double[] x, double[] y) {
		this.degree = degree;
		this.spanKnotIndex = spanKnotIndex;
		this.x = x;
		this.y = y;
	}

	/**
	 * @return degree {@code p}
	 */
	public int degree() {
		return degree;
	}

	/**
	 * @return knot index {@code j} of the span
	 */
	public int spanKnotIndex() {
		return spanKnotIndex;
	}

	/**
	 * @return a copy of the coefficients of x, index = power of {@code u}
	 */
	public double[] x() {
		return x.clone();
	}

	/**
	 * @return a copy of the coefficients of y, index = power of {@code u}
	 */
	public double[] y() {
		return y.clone();
	}

	/**
	 * @return whether every coefficient is finite
	 */
	public boolean allFinite() {
		for (int k = 0; k <= degree; k++) {
			if (!Double.isFinite(x[k]) || !Double.isFinite(y[k])) {
				return false;
			}
		}
		return true;
	}
}

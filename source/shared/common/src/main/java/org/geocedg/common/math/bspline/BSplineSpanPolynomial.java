/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import javax.annotation.CheckForNull;

/**
 * Immutable coefficients of the piece of one non-degenerate span {@code [u_j, u_{j+1}]} in the
 * span variable {@code s} in {@code [0, 1]}, {@code u = u_j + (u_{j+1} - u_j) s}, where the
 * difference is the exact difference of the two binary64 knots.
 *
 * <p>For {@link Form#BERNSTEIN} the coordinate functions are
 * {@code sum_i c_i B_{i,p}(s)}; for {@link Form#LOCAL_POWER} they are {@code sum_k c_k s^k};
 * for {@link Form#LOCAL_POWER_DESCENDING} they are {@code sum_i c_i s^(p-i)}.
 * For explicit weights the three arrays are homogeneous coefficients of {@code A_x},
 * {@code A_y} and {@code W}; the curve is {@code (A_x / W, A_y / W)}. For implicit unit
 * weights {@link #w()} is {@code null}.</p>
 */
public final class BSplineSpanPolynomial {

	/** Basis of the coefficients. */
	public enum Form {
		/** Bernstein basis of degree {@code p} in {@code s}. */
		BERNSTEIN,
		/** Monomials in ascending powers: index {@code k} holds the coefficient of {@code s^k}. */
		LOCAL_POWER,
		/**
		 * The same local power coefficients in descending powers: index {@code i} holds the
		 * coefficient of {@code s^(p-i)}, so the constant term is last.
		 */
		LOCAL_POWER_DESCENDING
	}

	private final Form form;
	private final int degree;
	private final int spanKnotIndex;
	private final double spanStart;
	private final double spanEnd;
	private final double[] x;
	private final double[] y;
	private final @CheckForNull double[] w;

	BSplineSpanPolynomial(Form form, int degree, int spanKnotIndex, double spanStart,
			double spanEnd, double[] x, double[] y, @CheckForNull double[] w) {
		this.form = form;
		this.degree = degree;
		this.spanKnotIndex = spanKnotIndex;
		this.spanStart = spanStart;
		this.spanEnd = spanEnd;
		this.x = x;
		this.y = y;
		this.w = w;
	}

	/**
	 * @return the basis of the coefficients
	 */
	public Form form() {
		return form;
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
	 * @return span start {@code u_j} ({@code s = 0})
	 */
	public double spanStart() {
		return spanStart;
	}

	/**
	 * @return span end {@code u_{j+1}} ({@code s = 1})
	 */
	public double spanEnd() {
		return spanEnd;
	}

	/**
	 * @return a copy of the {@code p + 1} coefficients of x (of {@code A_x} when rational)
	 */
	public double[] x() {
		return x.clone();
	}

	/**
	 * @return a copy of the {@code p + 1} coefficients of y (of {@code A_y} when rational)
	 */
	public double[] y() {
		return y.clone();
	}

	/**
	 * @return a copy of the {@code p + 1} coefficients of {@code W}, or {@code null} for
	 *         implicit unit weights
	 */
	public @CheckForNull double[] w() {
		return w == null ? null : w.clone();
	}
}

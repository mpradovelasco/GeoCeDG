/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Immutable result of a span lookup by the ownership rule of {@code bspline-2d} section 3.3:
 * a parameter equal to an interior knot value belongs to the span on its right, and
 * {@code u = b} belongs to the last non-degenerate span. The identification of {@code b}
 * with {@code a} for an exactly closed curve is a semantic decision of the consumer (the
 * kernel family maps {@code b} to {@code a} before the lookup); the library does not apply
 * it.
 */
public final class BSplineSpanLookup {

	private final BSplineStatus status;
	private final int spanKnotIndex;

	private BSplineSpanLookup(BSplineStatus status, int spanKnotIndex) {
		this.status = status;
		this.spanKnotIndex = spanKnotIndex;
	}

	static BSplineSpanLookup of(int spanKnotIndex) {
		return new BSplineSpanLookup(BSplineStatus.OK, spanKnotIndex);
	}

	static BSplineSpanLookup failure(BSplineStatus status) {
		return new BSplineSpanLookup(status, -1);
	}

	/**
	 * @return {@link BSplineStatus#OK}, {@link BSplineStatus#PARAMETER_NONFINITE} or
	 *         {@link BSplineStatus#PARAMETER_OUTSIDE_DOMAIN}
	 */
	public BSplineStatus status() {
		return status;
	}

	/**
	 * @return whether the parameter has an owning span
	 */
	public boolean isOk() {
		return status == BSplineStatus.OK;
	}

	/**
	 * @return knot index {@code j} of the owning span ({@code u_j < u_{j+1}}), or {@code -1}
	 */
	public int spanKnotIndex() {
		return spanKnotIndex;
	}
}

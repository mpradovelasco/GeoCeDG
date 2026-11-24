/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Immutable result of a point or derivative evaluation: a status, the two binary64
 * coordinates (both {@code NaN} unless the status is {@link BSplineStatus#OK}) and the knot
 * index {@code j} of the span whose polynomial piece was evaluated ({@code -1} on failure).
 */
public final class BSplinePoint {

	private final BSplineStatus status;
	private final double x;
	private final double y;
	private final int spanKnotIndex;

	private BSplinePoint(BSplineStatus status, double x, double y, int spanKnotIndex) {
		this.status = status;
		this.x = x;
		this.y = y;
		this.spanKnotIndex = spanKnotIndex;
	}

	static BSplinePoint of(double x, double y, int spanKnotIndex) {
		return new BSplinePoint(BSplineStatus.OK, x, y, spanKnotIndex);
	}

	static BSplinePoint failure(BSplineStatus status) {
		return new BSplinePoint(status, Double.NaN, Double.NaN, -1);
	}

	/**
	 * @return the status
	 */
	public BSplineStatus status() {
		return status;
	}

	/**
	 * @return whether the evaluation produced a value
	 */
	public boolean isOk() {
		return status == BSplineStatus.OK;
	}

	/**
	 * @return the x coordinate, {@code NaN} on failure
	 */
	public double x() {
		return x;
	}

	/**
	 * @return the y coordinate, {@code NaN} on failure
	 */
	public double y() {
		return y;
	}

	/**
	 * @return the knot index of the evaluated span, {@code -1} on failure
	 */
	public int spanKnotIndex() {
		return spanKnotIndex;
	}
}

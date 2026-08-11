/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Immutable finite world-coordinate point produced by semantic evaluation. */
public final class LocusPoint2D {
	private final double x;
	private final double y;

	/**
	 * @param x world x coordinate
	 * @param y world y coordinate
	 */
	public LocusPoint2D(double x, double y) {
		if (!Double.isFinite(x) || !Double.isFinite(y)) {
			throw new IllegalArgumentException("Locus V2 points must be finite");
		}
		this.x = normalizeZero(x);
		this.y = normalizeZero(y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusPoint2D)) {
			return false;
		}
		LocusPoint2D point = (LocusPoint2D) other;
		return Double.doubleToLongBits(x) == Double.doubleToLongBits(point.x)
				&& Double.doubleToLongBits(y) == Double.doubleToLongBits(point.y);
	}

	@Override
	public int hashCode() {
		long xBits = Double.doubleToLongBits(x);
		long yBits = Double.doubleToLongBits(y);
		return 31 * (int) (xBits ^ (xBits >>> 32))
				+ (int) (yBits ^ (yBits >>> 32));
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	private static double normalizeZero(double value) {
		return value == 0 ? 0 : value;
	}
}

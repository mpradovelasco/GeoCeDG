/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Immutable two-dimensional semantic coordinate value. */
public final class Vector2 {
	private final double x;
	private final double y;

	/**
	 * Creates a two-dimensional semantic value.
	 *
	 * @param x first coordinate
	 * @param y second coordinate
	 */
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public boolean isFinite() {
		return Double.isFinite(x) && Double.isFinite(y);
	}

	/** @return componentwise sum */
	public Vector2 add(Vector2 other) {
		return new Vector2(x + other.x, y + other.y);
	}

	/** @return componentwise difference */
	public Vector2 subtract(Vector2 other) {
		return new Vector2(x - other.x, y - other.y);
	}

	/** @return scalar multiple */
	public Vector2 scale(double factor) {
		return new Vector2(factor * x, factor * y);
	}

	/** @return Euclidean dot product */
	public double dot(Vector2 other) {
		return x * other.x + y * other.y;
	}

	/** @return signed two-dimensional cross product */
	public double cross(Vector2 other) {
		return x * other.y - y * other.x;
	}

	/** @return Euclidean norm */
	public double norm() {
		return Math.hypot(x, y);
	}

	/** @return unit vector with the same direction */
	public Vector2 normalized() {
		double magnitude = norm();
		if (!(magnitude > 0) || !Double.isFinite(magnitude)) {
			throw new IllegalStateException("cannot normalize a zero or non-finite vector");
		}
		return scale(1 / magnitude);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof Vector2)) {
			return false;
		}
		Vector2 other = (Vector2) object;
		return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
				&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
	}

	@Override
	public int hashCode() {
		long xBits = Double.doubleToLongBits(x);
		long yBits = Double.doubleToLongBits(y);
		return 31 * Long.hashCode(xBits) + Long.hashCode(yBits);
	}

	@Override
	public String toString() {
		return "Vector2[" + x + ", " + y + "]";
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Immutable three-dimensional semantic coordinate value. */
public final class Vector3 {
	private final double x;
	private final double y;
	private final double z;

	/**
	 * Creates a three-dimensional semantic value.
	 *
	 * @param x first coordinate
	 * @param y second coordinate
	 * @param z third coordinate
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public boolean isFinite() {
		return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
	}

	/** @return componentwise sum */
	public Vector3 add(Vector3 other) {
		return new Vector3(x + other.x, y + other.y, z + other.z);
	}

	/** @return componentwise difference */
	public Vector3 subtract(Vector3 other) {
		return new Vector3(x - other.x, y - other.y, z - other.z);
	}

	/** @return scalar multiple */
	public Vector3 scale(double factor) {
		return new Vector3(factor * x, factor * y, factor * z);
	}

	/** @return Euclidean dot product */
	public double dot(Vector3 other) {
		return x * other.x + y * other.y + z * other.z;
	}

	/** @return right-handed cross product */
	public Vector3 cross(Vector3 other) {
		return new Vector3(y * other.z - z * other.y,
				z * other.x - x * other.z,
				x * other.y - y * other.x);
	}

	/** @return Euclidean norm */
	public double norm() {
		return Math.sqrt(dot(this));
	}

	/** @return unit vector with the same direction */
	public Vector3 normalized() {
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
		if (!(object instanceof Vector3)) {
			return false;
		}
		Vector3 other = (Vector3) object;
		return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
				&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y)
				&& Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z);
	}

	@Override
	public int hashCode() {
		long xBits = Double.doubleToLongBits(x);
		long yBits = Double.doubleToLongBits(y);
		long zBits = Double.doubleToLongBits(z);
		return 31 * (31 * Long.hashCode(xBits) + Long.hashCode(yBits))
				+ Long.hashCode(zBits);
	}

	@Override
	public String toString() {
		return "Vector3[" + x + ", " + y + ", " + z + "]";
	}
}

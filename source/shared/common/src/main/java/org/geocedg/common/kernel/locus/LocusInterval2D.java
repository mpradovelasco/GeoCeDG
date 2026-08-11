/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Immutable oriented finite semantic-parameter interval. */
public final class LocusInterval2D {
	private final double lower;
	private final double upper;
	private final boolean lowerClosed;
	private final boolean upperClosed;

	/**
	 * @param lower lower endpoint
	 * @param upper upper endpoint
	 * @param lowerClosed whether lower endpoint belongs to the interval
	 * @param upperClosed whether upper endpoint belongs to the interval
	 */
	public LocusInterval2D(double lower, double upper, boolean lowerClosed,
			boolean upperClosed) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper) {
			throw new IllegalArgumentException("Invalid finite semantic interval");
		}
		if (lower == upper && !(lowerClosed && upperClosed)) {
			throw new IllegalArgumentException("An isolated value must be closed");
		}
		this.lower = normalizeZero(lower);
		this.upper = normalizeZero(upper);
		this.lowerClosed = lowerClosed;
		this.upperClosed = upperClosed;
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}

	public boolean isLowerClosed() {
		return lowerClosed;
	}

	public boolean isUpperClosed() {
		return upperClosed;
	}

	/**
	 * @param value canonical semantic parameter
	 * @param epsilon provider-owned endpoint tolerance
	 * @return whether the parameter is in this component
	 */
	public boolean contains(double value, double epsilon) {
		if (!Double.isFinite(value) || !Double.isFinite(epsilon) || epsilon < 0) {
			return false;
		}
		boolean above = lowerClosed ? value >= lower - epsilon
				: value > lower + epsilon;
		boolean below = upperClosed ? value <= upper + epsilon
				: value < upper - epsilon;
		return above && below;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusInterval2D)) {
			return false;
		}
		LocusInterval2D interval = (LocusInterval2D) other;
		return Double.doubleToLongBits(lower) == Double.doubleToLongBits(interval.lower)
				&& Double.doubleToLongBits(upper)
						== Double.doubleToLongBits(interval.upper)
				&& lowerClosed == interval.lowerClosed
				&& upperClosed == interval.upperClosed;
	}

	@Override
	public int hashCode() {
		long lowBits = Double.doubleToLongBits(lower);
		long highBits = Double.doubleToLongBits(upper);
		int hash = 31 * (int) (lowBits ^ (lowBits >>> 32))
				+ (int) (highBits ^ (highBits >>> 32));
		return 31 * hash + (lowerClosed ? 17 : 0) + (upperClosed ? 1 : 0);
	}

	@Override
	public String toString() {
		return (lowerClosed ? "[" : "(") + lower + ", " + upper
				+ (upperClosed ? "]" : ")");
	}

	private static double normalizeZero(double value) {
		return value == 0 ? 0 : value;
	}
}

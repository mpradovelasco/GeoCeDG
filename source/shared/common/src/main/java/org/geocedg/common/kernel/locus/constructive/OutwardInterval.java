/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * A closed interval of reals with binary64 bounds, computed with outward rounding, so the exact
 * result of an operation on any reals of the operands is enclosed: a sum moves a bound one
 * binary64 outward only when the exact rounding error of Knuth's TwoSum shows it was rounded
 * inward; every other operation rounds to nearest and then moves the lower bound one binary64
 * down and the upper bound one up. A product with the exact zero is the exact zero. A bound that
 * is not finite makes the interval unusable; every sign it is asked for is then undecided. It
 * decides nothing by itself: a sign is certified only when the whole interval lies on one side
 * of zero.
 */
public final class OutwardInterval {

	private final double lower;
	private final double upper;

	private OutwardInterval(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * @param value a binary64 value, its own exact definition
	 * @return the degenerate interval of the value
	 */
	public static OutwardInterval point(double value) {
		return new OutwardInterval(value, value);
	}

	/**
	 * @param lower lower bound
	 * @param upper upper bound, not below the lower one
	 * @return the interval
	 */
	public static OutwardInterval of(double lower, double upper) {
		if (!(lower <= upper)) {
			throw new IllegalArgumentException("Empty or undefined interval");
		}
		return new OutwardInterval(lower, upper);
	}

	/**
	 * @param value an exact rational
	 * @return the tightest binary64 interval that contains it
	 */
	public static OutwardInterval enclosing(ExactRational2D value) {
		double[] enclosure = value.outwardDoubleEnclosure();
		return new OutwardInterval(enclosure[0], enclosure[1]);
	}

	/** @return the lower bound */
	public double getLower() {
		return lower;
	}

	/** @return the upper bound */
	public double getUpper() {
		return upper;
	}

	/** @return whether both bounds are finite */
	public boolean isFinite() {
		return Double.isFinite(lower) && Double.isFinite(upper);
	}

	/** @return the width, an upper bound of the distance between two enclosed reals */
	public double width() {
		return Math.nextUp(upper - lower);
	}

	/**
	 * @return {@code 1} or {@code -1} when every enclosed real has that sign, {@code 0} for the
	 *         degenerate interval of zero, otherwise {@link ConstructiveArithmetic#UNDECIDED}
	 */
	public int certifiedSign() {
		if (!isFinite()) {
			return ConstructiveArithmetic.UNDECIDED;
		}
		if (lower > 0) {
			return 1;
		}
		if (upper < 0) {
			return -1;
		}
		return lower == 0 && upper == 0 ? 0 : ConstructiveArithmetic.UNDECIDED;
	}

	/**
	 * @param value a real given by its exact rational
	 * @return whether the interval contains it
	 */
	public boolean contains(ExactRational2D value) {
		return isFinite() && ExactRational2D.ofBinary64(lower).compareTo(value) <= 0
				&& value.compareTo(ExactRational2D.ofBinary64(upper)) <= 0;
	}

	/**
	 * @param other addend
	 * @return an enclosure of the sum
	 */
	public OutwardInterval add(OutwardInterval other) {
		if (other.isZero()) {
			return this;
		}
		if (isZero()) {
			return other;
		}
		return new OutwardInterval(sumDown(lower, other.lower), sumUp(upper, other.upper));
	}

	/**
	 * @param other subtrahend
	 * @return an enclosure of the difference
	 */
	public OutwardInterval subtract(OutwardInterval other) {
		return add(other.negate());
	}

	/** @return the exact opposite */
	public OutwardInterval negate() {
		return new OutwardInterval(-upper, -lower);
	}

	/**
	 * @param other factor
	 * @return an enclosure of the product
	 */
	public OutwardInterval multiply(OutwardInterval other) {
		if (isZero() || other.isZero()) {
			return point(0);
		}
		double p1 = lower * other.lower;
		double p2 = lower * other.upper;
		double p3 = upper * other.lower;
		double p4 = upper * other.upper;
		return outward(Math.min(Math.min(p1, p2), Math.min(p3, p4)),
				Math.max(Math.max(p1, p2), Math.max(p3, p4)));
	}

	/** @return an enclosure of the square, never below zero */
	public OutwardInterval square() {
		double a = Math.abs(lower);
		double b = Math.abs(upper);
		double high = Math.max(a, b);
		double low = lower <= 0 && upper >= 0 ? 0 : Math.min(a, b);
		OutwardInterval result = outward(low * low, high * high);
		return new OutwardInterval(Math.max(0, result.lower), result.upper);
	}

	/**
	 * @param other divisor whose interval does not contain zero
	 * @return an enclosure of the quotient
	 * @throws ArithmeticException when the divisor may vanish
	 */
	public OutwardInterval divide(OutwardInterval other) {
		if (!(other.lower > 0 || other.upper < 0)) {
			throw new ArithmeticException("Divisor interval contains zero");
		}
		double q1 = lower / other.lower;
		double q2 = lower / other.upper;
		double q3 = upper / other.lower;
		double q4 = upper / other.upper;
		return outward(Math.min(Math.min(q1, q2), Math.min(q3, q4)),
				Math.max(Math.max(q1, q2), Math.max(q3, q4)));
	}

	/**
	 * @return an enclosure of the nonnegative square root
	 * @throws ArithmeticException when the interval reaches below zero
	 */
	public OutwardInterval sqrt() {
		if (!(lower >= 0)) {
			throw new ArithmeticException("Square root of an interval below zero");
		}
		return outward(Math.sqrt(lower), Math.sqrt(upper));
	}

	/**
	 * @param other another interval
	 * @return the smallest interval that contains both
	 */
	public OutwardInterval hull(OutwardInterval other) {
		return new OutwardInterval(Math.min(lower, other.lower), Math.max(upper, other.upper));
	}

	private boolean isZero() {
		return lower == 0 && upper == 0;
	}

	/**
	 * Widens the rounded bounds by one binary64 each; a bound that is not finite gives the
	 * unusable interval, whose signs are all undecided.
	 */
	private static OutwardInterval outward(double low, double high) {
		if (!Double.isFinite(low) || !Double.isFinite(high)) {
			return new OutwardInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}
		return new OutwardInterval(Math.nextDown(low), Math.nextUp(high));
	}

	/** @return a lower bound of {@code a + b}, exact when the rounded sum is (Knuth's TwoSum) */
	private static double sumDown(double a, double b) {
		double sum = a + b;
		if (!Double.isFinite(sum)) {
			return Double.NEGATIVE_INFINITY;
		}
		return roundingError(a, b, sum) < 0 ? Math.nextDown(sum) : sum;
	}

	/** @return an upper bound of {@code a + b}, exact when the rounded sum is */
	private static double sumUp(double a, double b) {
		double sum = a + b;
		if (!Double.isFinite(sum)) {
			return Double.POSITIVE_INFINITY;
		}
		return roundingError(a, b, sum) > 0 ? Math.nextUp(sum) : sum;
	}

	/** @return the exact {@code a + b - sum} of a rounded finite sum */
	private static double roundingError(double a, double b, double sum) {
		double virtual = sum - a;
		return (a - (sum - virtual)) + (b - virtual);
	}

	@Override
	public String toString() {
		return "[" + lower + ", " + upper + "]";
	}
}

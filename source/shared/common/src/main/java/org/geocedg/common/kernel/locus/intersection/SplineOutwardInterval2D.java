/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * Finite closed intervals for the bounded spline-pair certificate. Every
 * nontrivial basic operation widens IEEE-754 round-to-nearest by one adjacent
 * representable number. Overflow, division through zero and nonfinite bounds
 * refuse a certificate; they never produce an artificial finite enclosure.
 */
final class SplineOutwardInterval2D {
	final double lower;
	final double upper;

	SplineOutwardInterval2D(double lower, double upper) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper) {
			throw new ArithmeticException("No finite interval enclosure");
		}
		this.lower = lower == 0 ? 0 : lower;
		this.upper = upper == 0 ? 0 : upper;
	}

	static SplineOutwardInterval2D point(double value) {
		return new SplineOutwardInterval2D(value, value);
	}

	SplineOutwardInterval2D add(SplineOutwardInterval2D other) {
		if (isZero()) {
			return other;
		}
		if (other.isZero()) {
			return this;
		}
		return outward(lower + other.lower, upper + other.upper);
	}

	SplineOutwardInterval2D subtract(SplineOutwardInterval2D other) {
		return add(other.negate());
	}

	SplineOutwardInterval2D negate() {
		return new SplineOutwardInterval2D(-upper, -lower);
	}

	SplineOutwardInterval2D multiply(SplineOutwardInterval2D other) {
		if (isZero() || other.isZero()) {
			return point(0);
		}
		if (lower == 1 && upper == 1) {
			return other;
		}
		if (other.lower == 1 && other.upper == 1) {
			return this;
		}
		double a = lower * other.lower;
		double b = lower * other.upper;
		double c = upper * other.lower;
		double d = upper * other.upper;
		return outward(Math.min(Math.min(a, b), Math.min(c, d)),
				Math.max(Math.max(a, b), Math.max(c, d)));
	}

	SplineOutwardInterval2D divide(SplineOutwardInterval2D other) {
		if (other.containsZero()) {
			throw new ArithmeticException("Interval divisor contains zero");
		}
		return multiply(outward(1 / other.upper, 1 / other.lower));
	}

	SplineOutwardInterval2D hull(SplineOutwardInterval2D other) {
		return new SplineOutwardInterval2D(Math.min(lower, other.lower),
				Math.max(upper, other.upper));
	}

	SplineOutwardInterval2D intersection(SplineOutwardInterval2D other) {
		double low = Math.max(lower, other.lower);
		double high = Math.min(upper, other.upper);
		return low <= high ? new SplineOutwardInterval2D(low, high) : null;
	}

	boolean containsZero() {
		return lower <= 0 && upper >= 0;
	}

	boolean contains(SplineOutwardInterval2D other) {
		return lower <= other.lower && upper >= other.upper;
	}

	boolean strictlyContains(SplineOutwardInterval2D other) {
		return lower < other.lower && upper > other.upper;
	}

	boolean disjoint(SplineOutwardInterval2D other) {
		return upper < other.lower || other.upper < lower;
	}

	double midpoint() {
		return Math.max(lower, Math.min(upper, lower / 2 + upper / 2));
	}

	double width() {
		return upper - lower;
	}

	private boolean isZero() {
		return lower == 0 && upper == 0;
	}

	private static SplineOutwardInterval2D outward(double low, double high) {
		return new SplineOutwardInterval2D(Math.nextDown(low), Math.nextUp(high));
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/**
 * Closed binary64 interval of the certified metric classes
 * ({@code locus-v2-metrics.md} section 25.1, N5). Every nontrivial operation widens the
 * round-to-nearest result by one adjacent binary64 value; operations with an
 * exact operand (zero, one, a halving of a normal number) stay exact. Overflow
 * and nonfinite bounds throw {@link ArithmeticException}: the certification fails
 * closed.
 */
final class MetricOutwardInterval2D {
	static final MetricOutwardInterval2D ZERO = point(0);
	static final MetricOutwardInterval2D ONE = point(1);

	final double lower;
	final double upper;

	MetricOutwardInterval2D(double lower, double upper) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper) {
			throw new ArithmeticException("No finite metric interval");
		}
		this.lower = lower == 0 ? 0 : lower;
		this.upper = upper == 0 ? 0 : upper;
	}

	static MetricOutwardInterval2D point(double value) {
		return new MetricOutwardInterval2D(value, value);
	}

	MetricOutwardInterval2D add(MetricOutwardInterval2D other) {
		if (isZero()) {
			return other;
		}
		if (other.isZero()) {
			return this;
		}
		return new MetricOutwardInterval2D(
				MetricOutwardArithmetic2D.addDown(lower, other.lower),
				MetricOutwardArithmetic2D.addUp(upper, other.upper));
	}

	MetricOutwardInterval2D subtract(MetricOutwardInterval2D other) {
		return add(other.negate());
	}

	MetricOutwardInterval2D negate() {
		return new MetricOutwardInterval2D(-upper, -lower);
	}

	MetricOutwardInterval2D multiply(MetricOutwardInterval2D other) {
		if (isZero() || other.isZero()) {
			return ZERO;
		}
		if (isOne()) {
			return other;
		}
		if (other.isOne()) {
			return this;
		}
		if (isMinusOne()) {
			return other.negate();
		}
		if (other.isMinusOne()) {
			return negate();
		}
		double low = Math.min(
				Math.min(MetricOutwardArithmetic2D.mulDown(lower, other.lower),
						MetricOutwardArithmetic2D.mulDown(lower, other.upper)),
				Math.min(MetricOutwardArithmetic2D.mulDown(upper, other.lower),
						MetricOutwardArithmetic2D.mulDown(upper, other.upper)));
		double high = Math.max(
				Math.max(MetricOutwardArithmetic2D.mulUp(lower, other.lower),
						MetricOutwardArithmetic2D.mulUp(lower, other.upper)),
				Math.max(MetricOutwardArithmetic2D.mulUp(upper, other.lower),
						MetricOutwardArithmetic2D.mulUp(upper, other.upper)));
		return new MetricOutwardInterval2D(low, high);
	}

	/** @return exact half for normal bounds, outward otherwise */
	MetricOutwardInterval2D half() {
		if (exactlyHalvable(lower) && exactlyHalvable(upper)) {
			return new MetricOutwardInterval2D(lower / 2, upper / 2);
		}
		return new MetricOutwardInterval2D(Math.nextDown(lower / 2),
				Math.nextUp(upper / 2));
	}

	/** @return enclosure of {@code x*x} for {@code x} in this interval */
	MetricOutwardInterval2D square() {
		double magnitude = magnitude();
		double high = magnitude == 0 ? 0
				: MetricOutwardArithmetic2D.mulUp(magnitude, magnitude);
		return new MetricOutwardInterval2D(
				MetricOutwardArithmetic2D.squareDown(mignitude()), high);
	}

	/** @return lower bound of {@code |x|} over this interval */
	double mignitude() {
		return MetricOutwardArithmetic2D.mignitude(lower, upper);
	}

	/** @return upper bound of {@code |x|} over this interval */
	double magnitude() {
		return MetricOutwardArithmetic2D.magnitude(lower, upper);
	}

	boolean isPoint() {
		return lower == upper;
	}

	private boolean isZero() {
		return lower == 0 && upper == 0;
	}

	private boolean isOne() {
		return lower == 1 && upper == 1;
	}

	private boolean isMinusOne() {
		return lower == -1 && upper == -1;
	}

	private static boolean exactlyHalvable(double value) {
		return value == 0 || Math.abs(value) >= 2 * Double.MIN_NORMAL;
	}

	@Override
	public String toString() {
		return "[" + lower + ", " + upper + "]";
	}
}

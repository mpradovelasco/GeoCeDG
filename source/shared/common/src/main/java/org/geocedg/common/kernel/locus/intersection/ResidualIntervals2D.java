/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Outward-rounded operations that the residual compositions of PRE-G9B-CU3 piece I-1 need. */
final class ResidualIntervals2D {

	private ResidualIntervals2D() {
		// Utility holder.
	}

	/** @return the enclosure of the binary64 value, its own exact definition */
	static SplineOutwardInterval2D constant(double value) {
		return SplineOutwardInterval2D.point(value);
	}

	/** @return an enclosure of the squares of the values, never below zero */
	static SplineOutwardInterval2D square(SplineOutwardInterval2D value) {
		double lower = value.lower;
		double upper = value.upper;
		if (lower >= 0) {
			return outward(lower * lower, upper * upper, false);
		}
		if (upper <= 0) {
			return outward(upper * upper, lower * lower, false);
		}
		return outward(0, Math.max(lower * lower, upper * upper), true);
	}

	/**
	 * @param value an enclosure whose negative part, if any, is rounding
	 * @return an enclosure of the nonnegative square roots of its nonnegative values
	 */
	static SplineOutwardInterval2D sqrt(SplineOutwardInterval2D value) {
		if (value.upper < 0) {
			throw new ArithmeticException("Square root of a negative enclosure");
		}
		double lower = value.lower <= 0 ? 0 : Math.max(0, Math.nextDown(Math.sqrt(value.lower)));
		return new SplineOutwardInterval2D(lower, Math.nextUp(Math.sqrt(value.upper)));
	}

	/** @return {@code first * x + second * y}, both factors binary64 constants */
	static SplineOutwardInterval2D combination(double first, SplineOutwardInterval2D x,
			double second, SplineOutwardInterval2D y) {
		return constant(first).multiply(x).add(constant(second).multiply(y));
	}

	private static SplineOutwardInterval2D outward(double lower, double upper,
			boolean exactLower) {
		return new SplineOutwardInterval2D(exactLower ? lower : Math.max(0,
				Math.nextDown(lower)), Math.nextUp(upper));
	}
}

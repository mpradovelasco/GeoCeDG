/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/**
 * Rigorous enclosure {@code [lower, upper]} of a semantic metric quantity
 * ({@code locus-v2-metrics.md} section 25.4). Its mathematical validity does not
 * depend on its width; a wide enclosure stays rigorous evidence.
 */
public final class MetricEnclosure2D {
	private final double lower;
	private final double upper;
	private final String method;

	/** Creates a finite, ordered, non-negative enclosure. */
	public MetricEnclosure2D(double lower, double upper, String method) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper
				|| upper < 0 || method == null || method.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid metric enclosure");
		}
		this.lower = Math.max(0, lower);
		this.upper = upper;
		this.method = method;
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}

	public String getMethod() {
		return method;
	}

	/** @return {@code upper - lower}, rounded up */
	public double getWidth() {
		return MetricOutwardArithmetic2D.subUp(upper, lower);
	}

	/** @return whether {@code value} lies inside, up to one rounding of the bounds */
	public boolean contains(double value) {
		return value >= Math.nextDown(lower) && value <= Math.nextUp(upper);
	}

	/**
	 * @return largest distance from {@code value} to the enclosure ends, rounded
	 *         up; the certified error of {@code value}
	 */
	public double certifiedErrorOf(double value) {
		return Math.max(MetricOutwardArithmetic2D.subUp(value, lower),
				MetricOutwardArithmetic2D.subUp(upper, value));
	}

	/** @return outward sum of two enclosures */
	public MetricEnclosure2D plus(MetricEnclosure2D other) {
		return new MetricEnclosure2D(
				MetricOutwardArithmetic2D.addDown(lower, other.lower),
				MetricOutwardArithmetic2D.addUp(upper, other.upper),
				method.equals(other.method) ? method : "sum of certified enclosures");
	}

	/** @return outward product with a non-negative factor enclosure */
	public MetricEnclosure2D times(double factorLower, double factorUpper,
			String transformedMethod) {
		return new MetricEnclosure2D(
				MetricOutwardArithmetic2D.mulDown(lower, factorLower),
				MetricOutwardArithmetic2D.mulUp(upper, factorUpper),
				transformedMethod);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricEnclosure2D)) {
			return false;
		}
		MetricEnclosure2D enclosure = (MetricEnclosure2D) other;
		return Double.doubleToLongBits(lower)
				== Double.doubleToLongBits(enclosure.lower)
				&& Double.doubleToLongBits(upper)
						== Double.doubleToLongBits(enclosure.upper)
				&& method.equals(enclosure.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Double.doubleToLongBits(lower),
				Double.doubleToLongBits(upper), method);
	}

	@Override
	public String toString() {
		return "[" + lower + ", " + upper + "] (" + method + ")";
	}
}

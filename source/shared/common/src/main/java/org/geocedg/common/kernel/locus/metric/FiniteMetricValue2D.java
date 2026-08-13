/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.OptionalDouble;

/** Finite non-negative total-variation value. */
public final class FiniteMetricValue2D implements MetricValue2D {
	private final double value;

	/** Creates a finite metric value; signed zero is canonicalized. */
	public FiniteMetricValue2D(double value) {
		if (!Double.isFinite(value) || value < 0) {
			throw new IllegalArgumentException(
					"A finite metric value must be finite and non-negative");
		}
		this.value = value == 0 ? 0 : value;
	}

	@Override
	public MetricValueKind getKind() {
		return MetricValueKind.FINITE;
	}

	@Override
	public OptionalDouble getFiniteValue() {
		return OptionalDouble.of(value);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof FiniteMetricValue2D
				&& Double.doubleToLongBits(value) == Double.doubleToLongBits(
						((FiniteMetricValue2D) other).value);
	}

	@Override
	public int hashCode() {
		return Double.hashCode(value);
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}
}

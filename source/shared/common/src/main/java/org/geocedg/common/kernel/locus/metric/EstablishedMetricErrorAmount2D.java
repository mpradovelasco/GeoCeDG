/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Established finite non-negative error amount. */
public final class EstablishedMetricErrorAmount2D implements MetricErrorAmount2D {
	private final double amount;

	/** Creates established evidence. */
	public EstablishedMetricErrorAmount2D(double amount) {
		if (!Double.isFinite(amount) || amount < 0) {
			throw new IllegalArgumentException(
					"An established error amount must be finite and non-negative");
		}
		this.amount = amount == 0 ? 0 : amount;
	}

	@Override
	public MetricErrorAmountKind getKind() {
		return MetricErrorAmountKind.ESTABLISHED;
	}

	public double getNonNegativeFiniteAmount() {
		return amount;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof EstablishedMetricErrorAmount2D
				&& Double.doubleToLongBits(amount) == Double.doubleToLongBits(
						((EstablishedMetricErrorAmount2D) other).amount);
	}

	@Override
	public int hashCode() {
		return Double.hashCode(amount);
	}
}

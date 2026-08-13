/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.OptionalDouble;

/** Established positive-infinite total variation. */
public final class PositiveInfinityMetricValue2D implements MetricValue2D {
	@Override
	public MetricValueKind getKind() {
		return MetricValueKind.POSITIVE_INFINITY;
	}

	@Override
	public OptionalDouble getFiniteValue() {
		return OptionalDouble.empty();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof PositiveInfinityMetricValue2D;
	}

	@Override
	public int hashCode() {
		return MetricValueKind.POSITIVE_INFINITY.hashCode();
	}

	@Override
	public String toString() {
		return "positive infinity";
	}
}

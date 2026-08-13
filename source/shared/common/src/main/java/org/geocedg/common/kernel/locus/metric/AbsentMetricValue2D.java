/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.OptionalDouble;

/** Metric value is unavailable; no numeric sentinel is carried. */
public final class AbsentMetricValue2D implements MetricValue2D {
	@Override
	public MetricValueKind getKind() {
		return MetricValueKind.ABSENT;
	}

	@Override
	public OptionalDouble getFiniteValue() {
		return OptionalDouble.empty();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof AbsentMetricValue2D;
	}

	@Override
	public int hashCode() {
		return MetricValueKind.ABSENT.hashCode();
	}

	@Override
	public String toString() {
		return "absent";
	}
}

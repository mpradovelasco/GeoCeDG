/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Error amount does not apply to an absent or infinite metric value. */
public final class NotApplicableMetricErrorAmount2D implements MetricErrorAmount2D {
	@Override
	public MetricErrorAmountKind getKind() {
		return MetricErrorAmountKind.NOT_APPLICABLE;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof NotApplicableMetricErrorAmount2D;
	}

	@Override
	public int hashCode() {
		return MetricErrorAmountKind.NOT_APPLICABLE.hashCode();
	}
}

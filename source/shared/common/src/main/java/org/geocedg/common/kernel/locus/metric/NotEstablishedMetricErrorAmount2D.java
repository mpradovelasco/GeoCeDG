/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Error evidence applies but no defensible numeric amount is established. */
public final class NotEstablishedMetricErrorAmount2D implements MetricErrorAmount2D {
	@Override
	public MetricErrorAmountKind getKind() {
		return MetricErrorAmountKind.NOT_ESTABLISHED;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof NotEstablishedMetricErrorAmount2D;
	}

	@Override
	public int hashCode() {
		return MetricErrorAmountKind.NOT_ESTABLISHED.hashCode();
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Closed error amount without NaN, magic values or nullable payloads. */
public sealed interface MetricErrorAmount2D permits EstablishedMetricErrorAmount2D,
		NotEstablishedMetricErrorAmount2D, NotApplicableMetricErrorAmount2D {
	/** @return closed error-amount discriminator */
	MetricErrorAmountKind getKind();
}

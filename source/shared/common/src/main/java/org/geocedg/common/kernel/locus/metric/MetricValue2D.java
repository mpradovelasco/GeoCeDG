/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.OptionalDouble;

/** Sentinel-free metric value. */
public sealed interface MetricValue2D permits FiniteMetricValue2D,
		PositiveInfinityMetricValue2D, AbsentMetricValue2D {
	/** @return closed value discriminator */
	MetricValueKind getKind();

	/** @return finite payload only for the finite variant */
	OptionalDouble getFiniteValue();
}

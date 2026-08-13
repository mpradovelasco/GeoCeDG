/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Semantic role of one component-confined route segment. */
public enum MetricRouteSegmentRole {
	DIRECT,
	PERIODIC_CYCLE,
	TO_GLOBAL_BOUNDARY,
	FROM_GLOBAL_BOUNDARY,
	COMPLETE_COMPONENT
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Route-resolution status before any metric integration. */
public enum MetricRouteStatus {
	RESOLVED,
	INVALID_QUERY,
	POSITION_STALE,
	DIFFERENT_LOCUS,
	DIFFERENT_BRANCH,
	DISCONTINUITY_ENCOUNTERED,
	TARGET_NOT_REACHABLE
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Outcome of a between-position traversal. Total queries have no outcome. */
public enum TraversalOutcome {
	TARGET_REACHED,
	STOPPED_AT_BOUNDARY,
	WRAPPED_TO_START,
	TARGET_NOT_REACHABLE,
	DISCONTINUITY_ENCOUNTERED
}

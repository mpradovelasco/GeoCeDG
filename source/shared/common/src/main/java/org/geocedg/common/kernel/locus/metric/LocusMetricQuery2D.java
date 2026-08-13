/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Closed internal query hierarchy; total length is not encoded with A/B. */
public sealed interface LocusMetricQuery2D permits BetweenPositionsMetricQuery,
		TotalLocusMetricQuery {
	/** @return stable source locus identity */
	String getLocusIdentity();

	/** @return coherent semantic revision */
	long getSemanticRevision();

	/** @return complete immutable metric policy */
	LocusMetricPolicy2D getPolicy();
}

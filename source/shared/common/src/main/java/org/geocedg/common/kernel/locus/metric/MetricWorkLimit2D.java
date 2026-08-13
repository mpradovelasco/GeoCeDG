/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Deterministic work limit that ended one metric call. */
public enum MetricWorkLimit2D {
	NONE,
	MAXIMUM_EVALUATIONS,
	MAXIMUM_SUBDIVISIONS,
	MAXIMUM_DEPTH
}

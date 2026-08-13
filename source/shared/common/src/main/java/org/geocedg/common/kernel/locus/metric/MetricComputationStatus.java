/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Computation status independent of value and rectifiability. */
public enum MetricComputationStatus {
	SUCCESS,
	INVALID_QUERY,
	UNSUPPORTED,
	NUMERICAL_FAILURE,
	LIMIT_NOT_ESTABLISHED
}

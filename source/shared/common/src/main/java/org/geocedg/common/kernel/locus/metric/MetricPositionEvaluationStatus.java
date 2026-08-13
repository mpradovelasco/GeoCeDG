/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Revision-bound semantic-position evaluation status. */
public enum MetricPositionEvaluationStatus {
	VALID,
	POSITION_STALE,
	POSITION_OUTSIDE_DOMAIN,
	BRANCH_MISSING,
	PROVIDER_VERSION_MISMATCH,
	EVALUATION_FAILED
}

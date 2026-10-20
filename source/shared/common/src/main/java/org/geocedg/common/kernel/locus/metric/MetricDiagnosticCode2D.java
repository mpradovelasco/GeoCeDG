/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Stable internal diagnostic codes for metric semantics and lifecycle. */
public enum MetricDiagnosticCode2D {
	EMPTY_DOMAIN,
	ISOLATED_POINT,
	COLLAPSED_IMAGE,
	POSITION_STALE,
	POSITION_OUTSIDE_DOMAIN,
	BRANCH_MISSING,
	PROVIDER_VERSION_MISMATCH,
	EVALUATION_FAILED,
	DIFFERENT_LOCUS,
	DIFFERENT_BRANCH,
	DISCONTINUITY,
	TARGET_NOT_REACHABLE,
	STOPPED_AT_BOUNDARY,
	WRAP_CONVENTION,
	UNSUPPORTED_CAPABILITY,
	NUMERICAL_FAILURE,
	LIMIT_NOT_ESTABLISHED,
	POSITIVE_INFINITY,
	NON_RECTIFIABLE,
	INCOMPLETE_AGGREGATE,
	GAP_PRESERVED,
	P1_FAILURE,
	WORK_BUDGET_EXHAUSTED,
	/** PRE-G9B-CU1 section 25.3e: a refinement consistency check failed persistently. */
	CONSISTENCY_NOT_ESTABLISHED,
	/** PRE-G9B-CU1 section 25.3e: refinement reached the parameter resolution. */
	PARAMETER_RESOLUTION_REACHED,
	/**
	 * PRE-G9B-CU1 sections 25.1 to 25.4: a certified capability failed closed or its
	 * enclosure is wider than the tolerance; informational.
	 */
	CERTIFICATION_NOT_ESTABLISHED
}

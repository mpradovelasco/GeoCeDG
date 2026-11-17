/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Role of metric evidence; never a render representation. */
public enum MetricRepresentationRole2D {
	SEMANTIC_METRIC,
	/** P2: entire declared numeric traversal, conditional on explicit hypotheses. */
	CONDITIONAL_NUMERIC_ESTIMATE,
	DERIVED_ARC_COORDINATE,
	DIAGNOSTIC_PARTIAL_VALUE
}

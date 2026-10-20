/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Numerical or analytic method used for total variation. */
public enum MetricMethod2D {
	CLOSED_FORM,
	ADAPTIVE_DIFFERENTIAL_QUADRATURE,
	ADAPTIVE_EVALUATOR_METRIC,
	IMPROPER_LIMIT,
	/** PRE-G9B-CU1 section 25.1 class 2: rigorous cone enclosure. */
	CERTIFIED_CONE_ENCLOSURE,
	NONE
}

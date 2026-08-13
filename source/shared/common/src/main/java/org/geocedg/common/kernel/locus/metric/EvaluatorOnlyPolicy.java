/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Truthful guarantee policy for point-evaluator-only metric work. */
public enum EvaluatorOnlyPolicy {
	UNCERTIFIED,
	ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS,
	UNSUPPORTED
}

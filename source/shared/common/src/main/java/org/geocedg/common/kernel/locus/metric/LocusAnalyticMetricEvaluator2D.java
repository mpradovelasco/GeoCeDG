/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Explicit analytic total-variation capability for one semantic locus family. */
@FunctionalInterface
public interface LocusAnalyticMetricEvaluator2D {
	/**
	 * @param branchKey constructive branch key
	 * @param startCanonicalParameter subarc start
	 * @param endCanonicalParameter subarc end
	 * @return analytic total-variation evidence
	 */
	LocusAnalyticMetricEvaluation2D evaluateLength(String branchKey,
			double startCanonicalParameter, double endCanonicalParameter);
}

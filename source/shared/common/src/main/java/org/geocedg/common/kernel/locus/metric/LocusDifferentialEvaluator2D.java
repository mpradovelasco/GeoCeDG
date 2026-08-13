/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;

/** Optional explicit differential capability; the G6 point evaluator is unchanged. */
public interface LocusDifferentialEvaluator2D {
	/**
	 * @param definition coherent semantic definition
	 * @param branchKey constructive branch
	 * @param providerCanonicalParameter provider-canonical parameter
	 * @param session scoped semantic session
	 * @return finite derivative evidence or typed invalid result
	 */
	LocusDifferentialEvaluation2D evaluateDifferential(
			LocusDefinition2D definition, String branchKey,
			double providerCanonicalParameter,
			LocusEvaluationSession2D session);
}

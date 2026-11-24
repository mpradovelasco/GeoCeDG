/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;

/**
 * PRE-G9B-MAINT-A2: optional additive extension of {@link LocusDifferentialEvaluator2D} for a
 * source whose first derivative may jump at an interior breakpoint (a {@code C^0} knot of
 * {@code bspline-2d}). It gives the derivative of the piece that ends at a parameter, the
 * limit from below. The metric estimator uses it only for the upper end of a piece; ownership
 * of the parameter, semantic addresses and every other consumer keep the inherited
 * {@link #evaluateDifferential} value. A source that does not implement it is measured
 * exactly as before.
 */
public interface OneSidedLocusDifferentialEvaluator2D extends LocusDifferentialEvaluator2D {
	/**
	 * @param definition coherent semantic definition
	 * @return whether this revision provides derivatives from below
	 */
	boolean supportsOneSidedDifferential(LocusDefinition2D definition);

	/**
	 * @param definition coherent semantic definition
	 * @param branchKey constructive branch
	 * @param providerCanonicalParameter parameter that ends a piece
	 * @param session scoped semantic session
	 * @return derivative of the piece that ends at the parameter, or a typed invalid result
	 */
	LocusDifferentialEvaluation2D evaluateDifferentialFromBelow(LocusDefinition2D definition,
			String branchKey, double providerCanonicalParameter,
			LocusEvaluationSession2D session);
}

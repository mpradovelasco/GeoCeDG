/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.spline.SplineSemanticEvaluator2D;

/**
 * Certified interval curve model of one semantic branch (ADR 0028). An
 * implementation encloses the branch's represented semantic curve and its first
 * derivative outward over any parameter box it accepts, and refuses a box by
 * throwing {@link ArithmeticException}. The model is certificate material only:
 * it never enters a selector, token or ledger entry.
 */
interface CertifiedIntervalCurveModel2D {

	/**
	 * @return outward enclosure of the value, or of the first derivative, over
	 *         the parameter box
	 */
	SplineOutwardInterval2D[] evaluate(SplineOutwardInterval2D parameter,
			boolean derivative);

	/** @return whether the represented curve is C1 throughout the box */
	boolean isSmooth(SplineOutwardInterval2D parameter);

	/** @return exact period of the component, or zero when none is claimed */
	double period(LocusInterval2D component);

	/** @return the provider's canonical parameter */
	double canonical(double parameter);

	/** @return computational partition of the domain; never identity */
	double[] getKnots();

	/**
	 * Captures the certified model of one branch, or {@code null} when the
	 * source exposes none. Structural splines keep their approved model; a
	 * reconstructible dependent-point locus qualifies only through the certified
	 * construction class.
	 *
	 * @return current model, or {@code null}
	 */
	static CertifiedIntervalCurveModel2D capture(LocusDefinition2D definition,
			String branch) {
		SplineIntervalModel2D.SimilarityChain chain =
				SplineIntervalModel2D.similarityChain(definition);
		if (chain == null) {
			return null;
		}
		Object root = chain.getRoot().getEvaluatorCapability();
		if (root instanceof SplineSemanticEvaluator2D) {
			return SplineIntervalModel2D.capture(definition, branch);
		}
		if (root instanceof ReconstructibleLocusEvaluator2D) {
			return ConstructionIntervalModel2D.capture(definition, chain, branch);
		}
		return null;
	}
}

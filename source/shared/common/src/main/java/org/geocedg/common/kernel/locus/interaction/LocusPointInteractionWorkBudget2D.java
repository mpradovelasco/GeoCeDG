/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;

/** Immutable ceilings for one inverse semantic-address query. */
public final class LocusPointInteractionWorkBudget2D {
	private final int maximumSemanticEvaluations;
	private final int maximumSubdivisions;
	private final int maximumRefinementIterations;
	private final int maximumCandidates;

	/** Creates one fully bounded query budget. */
	public LocusPointInteractionWorkBudget2D(int maximumSemanticEvaluations,
			int maximumSubdivisions, int maximumRefinementIterations,
			int maximumCandidates) {
		if (maximumSemanticEvaluations < 1 || maximumSubdivisions < 2
				|| maximumRefinementIterations < 1 || maximumCandidates < 1) {
			throw new IllegalArgumentException(
					"Locus point-interaction work must be positive and bounded");
		}
		this.maximumSemanticEvaluations = maximumSemanticEvaluations;
		this.maximumSubdivisions = maximumSubdivisions;
		this.maximumRefinementIterations = maximumRefinementIterations;
		this.maximumCandidates = maximumCandidates;
	}

	/** @return approved initial R6 ceilings */
	public static LocusPointInteractionWorkBudget2D initial() {
		return new LocusPointInteractionWorkBudget2D(32768, 512, 80, 1024);
	}

	public int getMaximumSemanticEvaluations() {
		return maximumSemanticEvaluations;
	}

	public int getMaximumSubdivisions() {
		return maximumSubdivisions;
	}

	public int getMaximumRefinementIterations() {
		return maximumRefinementIterations;
	}

	public int getMaximumCandidates() {
		return maximumCandidates;
	}

	/**
	 * @return deterministic evaluator-composition ceiling for this query; the
	 *         semantic-evaluation budget may only make the shared stack-safe
	 *         ceiling smaller
	 */
	public int getMaximumEvaluatorCompositionDepth() {
		return Math.min(maximumSemanticEvaluations,
				PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH);
	}
}

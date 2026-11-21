/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

/**
 * Semantic evaluator of one branch, as the conditioned traversal reads it. It is the
 * evaluator of the producer for one revision: deterministic, independent of any view, and
 * the only source of geometric facts of the evidence.
 */
public interface LocusConditionedSampler2D {
	/**
	 * @param canonicalParameter provider-canonical parameter inside the declared domain
	 * @return evaluation with its testimony
	 */
	LocusConditionedSample2D sample(double canonicalParameter);

	/**
	 * States whether a rigorous bound keeps the roots of every selecting dependency pairwise
	 * separated on a whole cell. The default has no such bound. A bound of this kind proves
	 * the separation of the roots it encloses and nothing else.
	 *
	 * @param lower lower end of the cell
	 * @param upper upper end of the cell
	 * @return whether the separation is bounded on the cell
	 */
	default boolean boundsSeparation(double lower, double upper) {
		return false;
	}
}

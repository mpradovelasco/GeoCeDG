/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Read-only, viewport-independent point evaluator for one semantic snapshot. */
public interface LocusEvaluator2D {
	/**
	 * Evaluates one already validated, provider-canonical parameter.
	 *
	 * @return typed semantic evaluation
	 */
	LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session);
}

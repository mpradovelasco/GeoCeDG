/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Internal deterministic construction evaluator used by the G6B factory. */
public interface LocusPointFunction2D {
	/** @return deterministic finite world-coordinate evaluation */
	LocusPoint2D evaluate(double capturedSourceValue, LocusBranch2D branch,
			double semanticParameter, LocusEvaluationSession2D session);
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Deterministic point evaluator over an immutable recompute source snapshot. */
public interface LocusDynamicPointFunction2D {
	/** @return deterministic finite world-coordinate evaluation */
	LocusPoint2D evaluate(LocusSourceSnapshot2D sources, LocusBranch2D branch,
			double semanticParameter, LocusEvaluationSession2D session);
}

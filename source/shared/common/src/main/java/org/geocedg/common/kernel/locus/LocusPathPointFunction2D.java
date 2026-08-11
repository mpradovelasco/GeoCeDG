/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Deterministic construction evaluated from a provider-owned path point. */
public interface LocusPathPointFunction2D {
	/** @return deterministic construction result for the semantic path point */
	LocusPoint2D evaluate(LocusPoint2D driverPoint, LocusBranch2D branch,
			double semanticParameter, LocusEvaluationSession2D session);
}

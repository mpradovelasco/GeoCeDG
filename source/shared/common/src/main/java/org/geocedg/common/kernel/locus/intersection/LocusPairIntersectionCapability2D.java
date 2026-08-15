/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Analytic, certified or evaluator-only pair candidate-isolation seam. */
public interface LocusPairIntersectionCapability2D {
	/** @return versioned capability identifier */
	String getCapabilityId();

	/** @return whether the capability accepts this exact captured pair */
	boolean supports(LocusPairIntersectionContext2D context);

	/** @return candidates plus truthful set-level evidence */
	LocusPairIntersectionCandidateSet2D isolate(
			LocusPairIntersectionContext2D context);
}

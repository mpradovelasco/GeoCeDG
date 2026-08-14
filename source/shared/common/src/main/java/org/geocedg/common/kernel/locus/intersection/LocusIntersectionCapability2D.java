/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Internal analytic, certified, derivative-aware or evaluator-only capability. */
public interface LocusIntersectionCapability2D {
	/** @return stable capability and proof-contract identifier */
	String getCapabilityId();

	/** @return whether this capability applies to the captured query */
	boolean supports(IntersectionCapabilityContext2D context);

	/**
	 * Isolates candidates and declares only the coverage it can establish.
	 * Every returned candidate is independently re-evaluated by the solver.
	 *
	 * @return immutable candidate and completeness evidence
	 */
	IntersectionCandidateSet2D isolate(IntersectionCapabilityContext2D context);
}

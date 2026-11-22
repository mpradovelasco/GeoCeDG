/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.Set;

/**
 * Closed list of the eligible causes of a conditioned absence, versioned with the policy
 * {@link LocusConditionedTraversalPolicy2D#VERSION_1} (contract {@code
 * post-r1-conditioned-use.md}, sections 3.4 and 5).
 *
 * <p>An absence is stated only by an undefined dependency whose own status states a
 * geometric absence. A computational failure, a work limit, an authority refusal, an
 * unreproduced selection, an unfaithful slice and a selection that is not established are
 * never in this list: their evaluation is a failure. A conditioned absence is never a
 * certified nonexistence.</p>
 */
public final class LocusConditionedAbsenceCause2D {
	/**
	 * The traced point is undefined, the evaluator found no computational failure, and every
	 * selecting dependency that the slice read has an established selection: an upstream
	 * construction states that its result does not exist at this parameter.
	 */
	public static final String DEPENDENCY_UNDEFINED = "DEPENDENCY_UNDEFINED";
	/**
	 * A token point of a Locus V2 intersection is undefined because the result that the
	 * slice computed for this evaluation succeeded, has no unresolved candidate and
	 * publishes no root for the token.
	 */
	public static final String INTERSECTION_ROOT_ABSENT = "INTERSECTION_ROOT_ABSENT";

	private static final Set<String> ELIGIBLE = Set.of(DEPENDENCY_UNDEFINED,
			INTERSECTION_ROOT_ABSENT);

	private LocusConditionedAbsenceCause2D() {
	}

	/**
	 * @param cause cause of an evaluation that is not valid
	 * @return whether the cause is in the closed list of the policy
	 */
	public static boolean isEligible(String cause) {
		return ELIGIBLE.contains(cause);
	}
}

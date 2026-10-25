/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;

/**
 * The decision configuration in which a public one-parameter query computes its certified
 * evidence ({@code locus-v2-intersections.md} CU3.4 and the constructive authority amendment):
 * the captured configuration with the binary64 target authority under
 * {@code legacy-representation-authority/v1}, the constructive configuration under
 * {@code constructive-authority/v1}. The G8B-R1 candidate search is the same in both.
 */
public interface IntersectionDecisionConfiguration2D {

	/** The captured configuration of {@code CU2} with the target adapter as authority. */
	IntersectionDecisionConfiguration2D CAPTURED = CapturedResidualJetSource2D::capture;

	/**
	 * @param definition the source of the query
	 * @param target the captured target of the query
	 * @return the residual of the query in this configuration, or {@code null} when it has no
	 *         enclosure there, so that no certified evidence is computed
	 */
	ResidualJetSource2D residualJets(LocusDefinition2D definition,
			LocusIntersectionTarget2D target);

	/**
	 * @return whether the admission of a root that G8B-R1 verifies is re-qualified under the
	 *         constructive authority ({@code locus-v2-constructive-authority.md} section 7.4)
	 */
	default boolean isConstructive() {
		return false;
	}
}

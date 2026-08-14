/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;

/** Captured authoritative target state for one query revision. */
public interface LocusIntersectionTarget2D {
	/** @return captured target family */
	TargetFamily getFamily();

	/** @return construction-scoped target identity */
	String getTargetIdentity();

	/** @return query-owned target update stamp */
	long getTargetUpdateStamp();

	/** @return typed normalized residual contract */
	IntersectionResidualContract2D getResidualContract();

	/** @return residual of a semantic point against captured target authority */
	TargetResidual2D evaluateResidual(LocusPoint2D point);

	/** @return limited-target membership evidence */
	TargetMembership2D evaluateMembership(LocusPoint2D point,
			double coordinateTolerance);

	/** @return normalized first-order contact evidence when established */
	TargetContactEvidence2D evaluateContact(LocusPoint2D point,
			LocusDifferentialEvaluation2D differential);
}

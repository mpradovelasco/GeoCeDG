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

	/** @return captured target domain, independent of any viewport */
	default IntersectionTargetDomain2D getDomainContract() {
		return IntersectionTargetDomain2D.allModelPlane(
				"Target family is defined over the model plane");
	}

	/**
	 * Evaluates the scalar used only for candidate isolation.
	 *
	 * <p>This value is not independent verification evidence.</p>
	 *
	 * @return typed candidate-level state
	 */
	default TargetCandidateEvaluation2D evaluateCandidateLevel(
			LocusPoint2D point) {
		try {
			TargetResidual2D residual = evaluateResidual(point);
			return TargetCandidateEvaluation2D.established(
					residual.getNormalizedResidual(),
					residual.getCharacteristicScale(),
					"Candidate level derived from the captured normalized target residual");
		} catch (IllegalArgumentException exception) {
			return TargetCandidateEvaluation2D.unavailable(
					IntersectionSemanticMetadata2D.TargetEvaluationStatus
							.RESIDUAL_NORMALIZATION_UNAVAILABLE,
					"Candidate residual unavailable: "
							+ exception.getClass().getSimpleName());
		}
	}

	/** @return residual of a semantic point against captured target authority */
	TargetResidual2D evaluateResidual(LocusPoint2D point);

	/** @return typed independent residual verification state */
	default TargetResidualEvaluation2D evaluateResidualEvidence(
			LocusPoint2D point) {
		try {
			return TargetResidualEvaluation2D.established(evaluateResidual(point),
					"Captured target residual evaluated independently");
		} catch (IllegalArgumentException exception) {
			return TargetResidualEvaluation2D.unavailable(
					IntersectionSemanticMetadata2D.TargetEvaluationStatus
							.RESIDUAL_NORMALIZATION_UNAVAILABLE,
					"Residual unavailable: "
							+ exception.getClass().getSimpleName());
		}
	}

	/** @return limited-target membership evidence */
	TargetMembership2D evaluateMembership(LocusPoint2D point,
			double coordinateTolerance);

	/** @return normalized first-order contact evidence when established */
	TargetContactEvidence2D evaluateContact(LocusPoint2D point,
			LocusDifferentialEvaluation2D differential);
}

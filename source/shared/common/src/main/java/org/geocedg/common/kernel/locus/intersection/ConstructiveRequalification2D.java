/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;

/**
 * The re-qualification of the roots that G8B-R1 admits, under {@code constructive-authority/v1}
 * ({@code locus-v2-constructive-authority.md} section 7.4): {@code STRICT} when the existence of
 * the root is certified against the constructive target, {@code RELAXED} by
 * {@code ENCLOSURE_COMPATIBLE}, otherwise {@code NOT_ESTABLISHED}. The adoption event computes it
 * for its impact report before anything is applied; it reads the published result and changes
 * no identity, token or selector.
 */
public final class ConstructiveRequalification2D {

	private ConstructiveRequalification2D() {
	}

	/**
	 * @param definition the published definition of the source
	 * @param target the captured target of the result
	 * @param source the residual in the constructive configuration, or {@code null} when the
	 *        source or the target has no enclosure there
	 * @param solution a solution that G8B-R1 admits
	 * @return its admission under the constructive authority
	 */
	public static ConstructiveAdmission of(LocusDefinition2D definition,
			LocusIntersectionTarget2D target, ResidualJetSource2D source,
			LocusIntersectionSolution2D solution) {
		if (source == null) {
			return ConstructiveAdmission.NOT_ESTABLISHED;
		}
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				definition.getProvider().getProviderId(),
				definition.getProvider().getParameterDescriptor(), target.getResidualContract());
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D(
				"constructive-requalification", "constructive-requalification",
				definition.getLocusIdentity(), Math.max(1, definition.getSemanticRevision()),
				target.getTargetIdentity(), target.getTargetUpdateStamp(),
				"constructive-requalification", policy);
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.openServiceSession(true,
				(int) Math.min(8_192, policy.getWorkBudget().getMaximumSemanticEvaluations()))) {
			IntersectionCapabilityContext2D context = new IntersectionCapabilityContext2D(query,
					definition, target, session,
					new LocusIntersectionInstrumentation2D(policy.getWorkBudget()));
			return CertifiedContactAnalysis2D.requalify(context, source, solution);
		}
	}
}

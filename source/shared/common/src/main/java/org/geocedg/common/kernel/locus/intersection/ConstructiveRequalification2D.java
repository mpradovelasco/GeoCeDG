/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;

/**
 * The re-qualification of the roots that G8B-R1 admits, under {@code constructive-authority/v1}
 * ({@code locus-v2-constructive-authority.md} section 7.4): {@code STRICT} when the existence of
 * the root is certified against the constructive target, {@code RELAXED} by
 * {@code ENCLOSURE_COMPATIBLE}, otherwise {@code NOT_ESTABLISHED}, and not admitted at all when the
 * conflict rule of the constructive configuration contradicts the root. The adoption event
 * computes it for its impact report before anything is applied; it reads the published result
 * and changes no identity, token or selector.
 */
public final class ConstructiveRequalification2D {

	private ConstructiveRequalification2D() {
	}

	/**
	 * The re-qualification of every published solution of one result, with the conflict rule
	 * of the constructive configuration applied to all of them together (PRE-G9B-CU3 increment
	 * E3): a solution that the rule withdraws, merges or reclassifies there is no longer
	 * admitted.
	 *
	 * @param definition the published definition of the source
	 * @param target the captured target of the result
	 * @param source the residual in the constructive configuration, or {@code null} when the
	 *        source or the target has no enclosure there
	 * @param solutions the published solutions of the result
	 * @return per solution, in the same order, its admission under the constructive authority,
	 *         or {@code null} when the conflict rule of the constructive configuration contradicts
	 *         it
	 */
	public static List<ConstructiveAdmission> ofResult(LocusDefinition2D definition,
			LocusIntersectionTarget2D target, ResidualJetSource2D source,
			List<LocusIntersectionSolution2D> solutions) {
		if (source == null) {
			return Collections.nCopies(solutions.size(), ConstructiveAdmission.NOT_ESTABLISHED);
		}
		LocusIntersectionPolicy2D policy = policy(definition, target);
		try (LocusEvaluationSession2D session = session(policy)) {
			return CertifiedContactAnalysis2D.requalify(context(definition, target, policy,
					session), source, solutions);
		}
	}

	private static LocusIntersectionPolicy2D policy(LocusDefinition2D definition,
			LocusIntersectionTarget2D target) {
		return LocusIntersectionPolicy2D.initial(definition.getProvider().getProviderId(),
				definition.getProvider().getParameterDescriptor(), target.getResidualContract());
	}

	private static LocusEvaluationSession2D session(LocusIntersectionPolicy2D policy) {
		return LocusEvaluationSession2D.openServiceSession(true,
				(int) Math.min(8_192, policy.getWorkBudget().getMaximumSemanticEvaluations()));
	}

	private static IntersectionCapabilityContext2D context(LocusDefinition2D definition,
			LocusIntersectionTarget2D target, LocusIntersectionPolicy2D policy,
			LocusEvaluationSession2D session) {
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D(
				"constructive-requalification", "constructive-requalification",
				definition.getLocusIdentity(), Math.max(1, definition.getSemanticRevision()),
				target.getTargetIdentity(), target.getTargetUpdateStamp(),
				"constructive-requalification", policy);
		return new IntersectionCapabilityContext2D(query, definition, target, session,
				new LocusIntersectionInstrumentation2D(policy.getWorkBudget()));
	}
}

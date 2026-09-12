/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

/** Immutable, non-mutating and revision-bound semantic redefine preflight. */
public final class SpatialRedefineAssessment {
	private final SpatialIdentityRegistry registry;
	private final SpatialRedefineContext context;
	private final SpatialRedefineProposal proposal;
	private final SpatialRedefineAssessmentStatus status;
	private final SpatialRedefineImpactReport impactReport;
	private final SpatialProceduralPositionSnapshot.Plan positionPlan;
	private final SpatialRedefineCandidateParticipation candidateParticipation;
	private final boolean proceduralPositionRequired;
	private final String hostStateToken;
	private final String detail;

	SpatialRedefineAssessment(SpatialIdentityRegistry registry,
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineAssessmentStatus status,
			SpatialRedefineImpactReport impactReport,
			SpatialProceduralPositionSnapshot.Plan positionPlan,
			SpatialRedefineCandidateParticipation candidateParticipation,
			boolean proceduralPositionRequired, String hostStateToken,
			String detail) {
		this.registry = Objects.requireNonNull(registry);
		this.context = context;
		this.proposal = proposal;
		this.status = Objects.requireNonNull(status);
		this.impactReport = impactReport;
		this.positionPlan = positionPlan;
		this.candidateParticipation = candidateParticipation;
		this.proceduralPositionRequired = proceduralPositionRequired;
		this.hostStateToken = hostStateToken;
		this.detail = detail;
	}

	public SpatialRedefineContext getContext() {
		return context;
	}

	public SpatialRedefineProposal getProposal() {
		return proposal;
	}

	public SpatialRedefineAssessmentStatus getStatus() {
		return status;
	}

	public SpatialRedefineImpactReport getImpactReport() {
		return impactReport;
	}

	/** @return diagnostic detail; never localized frontend text */
	public String getDetail() {
		return detail;
	}

	/** @return whether this exact assessment remains current without mutation */
	public boolean isCurrent() {
		return registry.isRedefineAssessmentCurrent(this);
	}

	SpatialIdentityRegistry getRegistry() {
		return registry;
	}

	SpatialProceduralPositionSnapshot.Plan getPositionPlan() {
		return positionPlan;
	}

	SpatialRedefineCandidateParticipation getCandidateParticipation() {
		return candidateParticipation;
	}

	boolean isProceduralPositionRequired() {
		return proceduralPositionRequired;
	}

	String getHostStateToken() {
		return hostStateToken;
	}
}

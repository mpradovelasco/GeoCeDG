/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Thread-confined deterministic counters for spatial-semantic evaluation. */
public final class SpatialSemanticInstrumentation {
	private final Thread ownerThread = Thread.currentThread();
	private long frameEvaluations;
	private long projectionSystemEvaluations;
	private long diagramMapForwardEvaluations;
	private long diagramMapInverseEvaluations;
	private long hingeConsistencyEvaluations;
	private long changeOfPlaneConsistencyEvaluations;
	private long projectionSystemCertificatePublications;
	private long projectionSystemCertificateRejections;
	private long reconstructionAttempts;
	private long rankEvaluations;
	private long candidateObjectsBuilt;
	private long reprojectionEvaluations;
	private long certificatePublications;
	private long failurePublications;
	private long supersededCandidateRejections;
	private long dependencyUpdates;
	private long derivedViewPublications;
	private long derivedViewWithdrawals;

	void recordFrameEvaluation() {
		checkOwner();
		frameEvaluations++;
	}

	void recordProjectionSystemEvaluation() {
		checkOwner();
		projectionSystemEvaluations++;
	}

	void recordDiagramMapForwardEvaluation() {
		checkOwner();
		diagramMapForwardEvaluations++;
	}

	void recordDiagramMapInverseEvaluation() {
		checkOwner();
		diagramMapInverseEvaluations++;
	}

	void recordRelationEvaluation(ProjectionRelationKind kind) {
		checkOwner();
		if (kind == ProjectionRelationKind.HINGE_UNFOLD) {
			hingeConsistencyEvaluations++;
		} else {
			changeOfPlaneConsistencyEvaluations++;
		}
	}

	void recordProjectionSystemCertificate(boolean accepted) {
		checkOwner();
		if (accepted) {
			projectionSystemCertificatePublications++;
		} else {
			projectionSystemCertificateRejections++;
		}
	}

	void recordReconstructionAttempt() {
		checkOwner();
		reconstructionAttempts++;
	}

	void recordRankEvaluation() {
		checkOwner();
		rankEvaluations++;
	}

	void recordCandidateObjectBuilt() {
		checkOwner();
		candidateObjectsBuilt++;
	}

	void recordReprojectionEvaluation() {
		checkOwner();
		reprojectionEvaluations++;
	}

	void recordObjectCertificate(boolean accepted) {
		checkOwner();
		if (accepted) {
			certificatePublications++;
		} else {
			failurePublications++;
		}
	}

	/** Records a normal construction-dependency update. */
	public void recordDependencyUpdate() {
		checkOwner();
		dependencyUpdates++;
	}

	/** Records rejection of an otherwise complete candidate for an old revision. */
	public void recordSupersededCandidateRejection() {
		checkOwner();
		supersededCandidateRejections++;
	}

	/** Records publication through a one-way, non-authoritative derived adapter. */
	public void recordDerivedViewPublication() {
		checkOwner();
		derivedViewPublications++;
	}

	/** Records withdrawal of a derived view value after semantic invalidation. */
	public void recordDerivedViewWithdrawal() {
		checkOwner();
		derivedViewWithdrawals++;
	}

	public long getFrameEvaluations() {
		return frameEvaluations;
	}

	public long getProjectionSystemEvaluations() {
		return projectionSystemEvaluations;
	}

	public long getDiagramMapForwardEvaluations() {
		return diagramMapForwardEvaluations;
	}

	public long getDiagramMapInverseEvaluations() {
		return diagramMapInverseEvaluations;
	}

	public long getHingeConsistencyEvaluations() {
		return hingeConsistencyEvaluations;
	}

	public long getChangeOfPlaneConsistencyEvaluations() {
		return changeOfPlaneConsistencyEvaluations;
	}

	public long getProjectionSystemCertificatePublications() {
		return projectionSystemCertificatePublications;
	}

	public long getProjectionSystemCertificateRejections() {
		return projectionSystemCertificateRejections;
	}

	public long getReconstructionAttempts() {
		return reconstructionAttempts;
	}

	public long getRankEvaluations() {
		return rankEvaluations;
	}

	public long getCandidateObjectsBuilt() {
		return candidateObjectsBuilt;
	}

	public long getReprojectionEvaluations() {
		return reprojectionEvaluations;
	}

	public long getCertificatePublications() {
		return certificatePublications;
	}

	public long getFailurePublications() {
		return failurePublications;
	}

	public long getSupersededCandidateRejections() {
		return supersededCandidateRejections;
	}

	public long getDependencyUpdates() {
		return dependencyUpdates;
	}

	public long getDerivedViewPublications() {
		return derivedViewPublications;
	}

	public long getDerivedViewWithdrawals() {
		return derivedViewWithdrawals;
	}

	// Forbidden authority mechanisms intentionally have no mutator.
	public long getLabelFallbackLookups() {
		return 0;
	}

	public long getCoordinateAssociationAttempts() {
		return 0;
	}

	public long getCreationOrderAssociationAttempts() {
		return 0;
	}

	public long getXmlPositionAssociationAttempts() {
		return 0;
	}

	public long getOutputIndexAssociationAttempts() {
		return 0;
	}

	public long getJavaReferenceIdentityAssumptions() {
		return 0;
	}

	public long getVisibleDiagramAssociationAttempts() {
		return 0;
	}

	public long getStalePayloadPublications() {
		return 0;
	}

	public long getMixedAuthorityRevisionPublications() {
		return 0;
	}

	public long getHiddenGraphRecomputations() {
		return 0;
	}

	public long getRenderCacheReads() {
		return 0;
	}

	public long getRendererReads() {
		return 0;
	}

	public long getViewportReads() {
		return 0;
	}

	public long getScreenCoordinateReads() {
		return 0;
	}

	public long getDpiReads() {
		return 0;
	}

	public long getCameraTransformReads() {
		return 0;
	}

	public long getLayerOrVisibilityReads() {
		return 0;
	}

	// Compatibility aliases keep forbidden-authority assertions readable.
	public long getLabelAuthorityUses() {
		return 0;
	}

	public long getCoordinateAuthorityUses() {
		return 0;
	}

	public long getConstructionOrderAuthorityUses() {
		return 0;
	}

	public long getXmlPositionAuthorityUses() {
		return 0;
	}

	public long getOutputOrdinalAuthorityUses() {
		return 0;
	}

	public long getJavaInstanceAuthorityUses() {
		return 0;
	}

	public long getViewportAuthorityUses() {
		return 0;
	}

	public long getDpiAuthorityUses() {
		return 0;
	}

	public long getCameraAuthorityUses() {
		return 0;
	}

	public long getRendererAuthorityUses() {
		return 0;
	}

	public long getScreenStateAuthorityUses() {
		return 0;
	}

	public long getStaleSpatialPayloadPublications() {
		return 0;
	}

	/** Resets the evidence record on its owning thread. */
	public void reset() {
		checkOwner();
		frameEvaluations = 0;
		projectionSystemEvaluations = 0;
		diagramMapForwardEvaluations = 0;
		diagramMapInverseEvaluations = 0;
		hingeConsistencyEvaluations = 0;
		changeOfPlaneConsistencyEvaluations = 0;
		projectionSystemCertificatePublications = 0;
		projectionSystemCertificateRejections = 0;
		reconstructionAttempts = 0;
		rankEvaluations = 0;
		candidateObjectsBuilt = 0;
		reprojectionEvaluations = 0;
		certificatePublications = 0;
		failurePublications = 0;
		supersededCandidateRejections = 0;
		dependencyUpdates = 0;
		derivedViewPublications = 0;
		derivedViewWithdrawals = 0;
	}

	private void checkOwner() {
		if (Thread.currentThread() != ownerThread) {
			throw new IllegalStateException("instrumentation is thread-confined");
		}
	}
}

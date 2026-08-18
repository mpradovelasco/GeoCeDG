/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;

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
	private long authoritativePublicationEpoch;
	private final Map<SpatialIdentityId, Long> authoritativePublicationCounts =
			new LinkedHashMap<>();

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

	/** Records one typed authoritative system or object certificate publication. */
	public void recordAuthoritativePublication(SpatialIdentityId subject) {
		checkOwner();
		if (subject == null) {
			throw new IllegalArgumentException(
					"Authoritative publication subject must be typed");
		}
		long nextEpoch = Math.addExact(authoritativePublicationEpoch, 1);
		long nextSubjectCount = Math.addExact(
				authoritativePublicationCounts.getOrDefault(subject, 0L), 1);
		authoritativePublicationEpoch = nextEpoch;
		authoritativePublicationCounts.put(subject, nextSubjectCount);
	}

	/** @return monotone epoch unaffected by evidence-counter resets */
	public long getAuthoritativePublicationEpoch() {
		return authoritativePublicationEpoch;
	}

	/** @return immutable same-thread subject counts for publication-scope checks */
	public Map<SpatialIdentityId, Long> snapshotAuthoritativePublicationCounts() {
		checkOwner();
		return Collections.unmodifiableMap(
				new LinkedHashMap<>(authoritativePublicationCounts));
	}

	/**
	 * Checks that a staged evidence merge can complete without changing the live
	 * counters. The lifecycle runtime calls this before publishing used geo types,
	 * so an overflow cannot leave construction compatibility state behind.
	 *
	 * @param staged same-thread lifecycle evidence
	 */
	public void preflightMergeFrom(SpatialSemanticInstrumentation staged) {
		checkOwner();
		staged.checkOwner();
		if (staged == this) {
			return;
		}
		Math.addExact(frameEvaluations, staged.frameEvaluations);
		Math.addExact(projectionSystemEvaluations,
				staged.projectionSystemEvaluations);
		Math.addExact(diagramMapForwardEvaluations,
				staged.diagramMapForwardEvaluations);
		Math.addExact(diagramMapInverseEvaluations,
				staged.diagramMapInverseEvaluations);
		Math.addExact(hingeConsistencyEvaluations,
				staged.hingeConsistencyEvaluations);
		Math.addExact(changeOfPlaneConsistencyEvaluations,
				staged.changeOfPlaneConsistencyEvaluations);
		Math.addExact(projectionSystemCertificatePublications,
				staged.projectionSystemCertificatePublications);
		Math.addExact(projectionSystemCertificateRejections,
				staged.projectionSystemCertificateRejections);
		Math.addExact(reconstructionAttempts, staged.reconstructionAttempts);
		Math.addExact(rankEvaluations, staged.rankEvaluations);
		Math.addExact(candidateObjectsBuilt, staged.candidateObjectsBuilt);
		Math.addExact(reprojectionEvaluations, staged.reprojectionEvaluations);
		Math.addExact(certificatePublications, staged.certificatePublications);
		Math.addExact(failurePublications, staged.failurePublications);
		Math.addExact(supersededCandidateRejections,
				staged.supersededCandidateRejections);
		Math.addExact(dependencyUpdates, staged.dependencyUpdates);
		Math.addExact(derivedViewPublications, staged.derivedViewPublications);
		Math.addExact(derivedViewWithdrawals, staged.derivedViewWithdrawals);
		Math.addExact(authoritativePublicationEpoch,
				staged.authoritativePublicationEpoch);
		for (Map.Entry<SpatialIdentityId, Long> entry
				: staged.authoritativePublicationCounts.entrySet()) {
			Math.addExact(authoritativePublicationCounts.getOrDefault(
					entry.getKey(), 0L), entry.getValue());
		}
	}

	/**
	 * Atomically incorporates successful lifecycle-preparation evidence.
	 * A staged runtime uses a separate counter sink so an abandoned or failed
	 * preparation cannot alter the live evidence record.
	 *
	 * @param staged successful same-thread lifecycle evidence
	 */
	public void mergeFrom(SpatialSemanticInstrumentation staged) {
		preflightMergeFrom(staged);
		if (staged == this) {
			return;
		}
		long mergedFrameEvaluations = Math.addExact(frameEvaluations,
				staged.frameEvaluations);
		long mergedProjectionSystemEvaluations = Math.addExact(
				projectionSystemEvaluations, staged.projectionSystemEvaluations);
		long mergedDiagramMapForwardEvaluations = Math.addExact(
				diagramMapForwardEvaluations, staged.diagramMapForwardEvaluations);
		long mergedDiagramMapInverseEvaluations = Math.addExact(
				diagramMapInverseEvaluations, staged.diagramMapInverseEvaluations);
		long mergedHingeConsistencyEvaluations = Math.addExact(
				hingeConsistencyEvaluations, staged.hingeConsistencyEvaluations);
		long mergedChangeOfPlaneConsistencyEvaluations = Math.addExact(
				changeOfPlaneConsistencyEvaluations,
				staged.changeOfPlaneConsistencyEvaluations);
		long mergedProjectionSystemCertificatePublications = Math.addExact(
				projectionSystemCertificatePublications,
				staged.projectionSystemCertificatePublications);
		final long mergedProjectionSystemCertificateRejections = Math.addExact(
				projectionSystemCertificateRejections,
				staged.projectionSystemCertificateRejections);
		final long mergedReconstructionAttempts = Math.addExact(reconstructionAttempts,
				staged.reconstructionAttempts);
		final long mergedRankEvaluations = Math.addExact(rankEvaluations,
				staged.rankEvaluations);
		final long mergedCandidateObjectsBuilt = Math.addExact(candidateObjectsBuilt,
				staged.candidateObjectsBuilt);
		final long mergedReprojectionEvaluations = Math.addExact(reprojectionEvaluations,
				staged.reprojectionEvaluations);
		final long mergedCertificatePublications = Math.addExact(certificatePublications,
				staged.certificatePublications);
		final long mergedFailurePublications = Math.addExact(failurePublications,
				staged.failurePublications);
		final long mergedSupersededCandidateRejections = Math.addExact(
				supersededCandidateRejections, staged.supersededCandidateRejections);
		final long mergedDependencyUpdates = Math.addExact(dependencyUpdates,
				staged.dependencyUpdates);
		final long mergedDerivedViewPublications = Math.addExact(derivedViewPublications,
				staged.derivedViewPublications);
		final long mergedDerivedViewWithdrawals = Math.addExact(derivedViewWithdrawals,
				staged.derivedViewWithdrawals);
		final long mergedAuthoritativePublicationEpoch = Math.addExact(
				authoritativePublicationEpoch,
				staged.authoritativePublicationEpoch);

		frameEvaluations = mergedFrameEvaluations;
		projectionSystemEvaluations = mergedProjectionSystemEvaluations;
		diagramMapForwardEvaluations = mergedDiagramMapForwardEvaluations;
		diagramMapInverseEvaluations = mergedDiagramMapInverseEvaluations;
		hingeConsistencyEvaluations = mergedHingeConsistencyEvaluations;
		changeOfPlaneConsistencyEvaluations =
				mergedChangeOfPlaneConsistencyEvaluations;
		projectionSystemCertificatePublications =
				mergedProjectionSystemCertificatePublications;
		projectionSystemCertificateRejections =
				mergedProjectionSystemCertificateRejections;
		reconstructionAttempts = mergedReconstructionAttempts;
		rankEvaluations = mergedRankEvaluations;
		candidateObjectsBuilt = mergedCandidateObjectsBuilt;
		reprojectionEvaluations = mergedReprojectionEvaluations;
		certificatePublications = mergedCertificatePublications;
		failurePublications = mergedFailurePublications;
		supersededCandidateRejections = mergedSupersededCandidateRejections;
		dependencyUpdates = mergedDependencyUpdates;
		derivedViewPublications = mergedDerivedViewPublications;
		derivedViewWithdrawals = mergedDerivedViewWithdrawals;
		authoritativePublicationEpoch = mergedAuthoritativePublicationEpoch;
		for (Map.Entry<SpatialIdentityId, Long> entry
				: staged.authoritativePublicationCounts.entrySet()) {
			authoritativePublicationCounts.put(entry.getKey(), Math.addExact(
					authoritativePublicationCounts.getOrDefault(entry.getKey(), 0L),
					entry.getValue()));
		}
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

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable atomic result of one projection-defined point evaluation. */
public final class SpatialPointCertificate {
	private final SpatialCapabilityStatus capabilityStatus;
	private final ProjectionSystemCertificate projectionSystemCertificate;
	private final SpatialDefinitionStatus definitionStatus;
	private final SpatialCertificateStatus certificateStatus;
	private final SpatialCurrentnessStatus currentnessStatus;
	private final RepresentationFidelity representationFidelity;
	private final NumericalEvidenceStatus numericalEvidenceStatus;
	private final CorrespondenceStatus correspondenceStatus;
	private final long sourceRevision;
	private final Optional<Vector3> point;
	private final int rank;
	private final double[] singularValues;
	private final double conditionNumber;
	private final List<ProjectionResidualEvidence> residualEvidence;
	private final double maximumIntrinsicNormalizedResidual;
	private final double maximumDiagramNormalizedResidual;
	private final String arithmeticMethod;

	/**
	 * Creates one atomic projection-defined point result.
	 *
	 * @param capabilityStatus support status of the point vocabulary
	 * @param projectionSystemCertificate required system subcertificate
	 * @param definitionStatus input definition status
	 * @param certificateStatus point certificate status
	 * @param currentnessStatus source-currentness status
	 * @param representationFidelity worst participating fidelity
	 * @param numericalEvidenceStatus numerical evidence classification
	 * @param correspondenceStatus point correspondence status
	 * @param sourceRevision maximum participating source revision
	 * @param point current valid point payload, when present
	 * @param rank evaluated projection-matrix rank
	 * @param singularValues evaluated singular values
	 * @param conditionNumber evaluated condition number
	 * @param residualEvidence per-observation residual evidence
	 * @param maximumIntrinsicNormalizedResidual maximum intrinsic residual
	 * @param maximumDiagramNormalizedResidual maximum common-diagram residual
	 * @param arithmeticMethod declared arithmetic method
	 */
	public SpatialPointCertificate(SpatialCapabilityStatus capabilityStatus,
			ProjectionSystemCertificate projectionSystemCertificate,
			SpatialDefinitionStatus definitionStatus,
			SpatialCertificateStatus certificateStatus,
			SpatialCurrentnessStatus currentnessStatus,
			RepresentationFidelity representationFidelity,
			NumericalEvidenceStatus numericalEvidenceStatus,
			CorrespondenceStatus correspondenceStatus, long sourceRevision,
			Optional<Vector3> point, int rank, double[] singularValues,
			double conditionNumber,
			List<ProjectionResidualEvidence> residualEvidence,
			double maximumIntrinsicNormalizedResidual,
			double maximumDiagramNormalizedResidual, String arithmeticMethod) {
		this.capabilityStatus = Objects.requireNonNull(capabilityStatus);
		this.projectionSystemCertificate = Objects.requireNonNull(
				projectionSystemCertificate);
		this.definitionStatus = Objects.requireNonNull(definitionStatus);
		this.certificateStatus = Objects.requireNonNull(certificateStatus);
		this.currentnessStatus = Objects.requireNonNull(currentnessStatus);
		this.representationFidelity = Objects.requireNonNull(representationFidelity);
		this.numericalEvidenceStatus = Objects.requireNonNull(numericalEvidenceStatus);
		this.correspondenceStatus = Objects.requireNonNull(correspondenceStatus);
		this.sourceRevision = SemanticValueChecks.requireRevision(sourceRevision);
		this.point = Objects.requireNonNull(point);
		if (rank < 0) {
			throw new IllegalArgumentException("rank must be non-negative");
		}
		this.rank = rank;
		this.singularValues = Objects.requireNonNull(singularValues).clone();
		this.conditionNumber = conditionNumber;
		ArrayList<ProjectionResidualEvidence> canonicalResiduals = new ArrayList<>(
				Objects.requireNonNull(residualEvidence));
		canonicalResiduals.sort(Comparator.comparing(
				ProjectionResidualEvidence::getEvidenceKey));
		requireDistinctResidualKeys(canonicalResiduals);
		this.residualEvidence = Collections.unmodifiableList(canonicalResiduals);
		this.maximumIntrinsicNormalizedResidual = maximumIntrinsicNormalizedResidual;
		this.maximumDiagramNormalizedResidual = maximumDiagramNormalizedResidual;
		this.arithmeticMethod = Objects.requireNonNull(arithmeticMethod);
		if ((certificateStatus == SpatialCertificateStatus.VALID) != point.isPresent()) {
			throw new IllegalArgumentException(
					"only a valid certificate may carry a spatial point payload");
		}
		if (point.isPresent() && currentnessStatus != SpatialCurrentnessStatus.CURRENT) {
			throw new IllegalArgumentException("a spatial point payload must be current");
		}
		if (capabilityStatus == SpatialCapabilityStatus.UNSUPPORTED
				&& certificateStatus != SpatialCertificateStatus.NOT_EVALUATED) {
			throw new IllegalArgumentException(
					"an unsupported point capability must remain not evaluated");
		}
		if (certificateStatus == SpatialCertificateStatus.VALID
				&& (projectionSystemCertificate.getStatus()
						!= ProjectionSystemStatus.CONSISTENT
						|| definitionStatus != SpatialDefinitionStatus.DEFINED)) {
			throw new IllegalArgumentException(
					"a valid point requires a defined, consistent system context");
		}
		if (certificateStatus == SpatialCertificateStatus.VALID
				&& !hasFiniteValidPayload()) {
			throw new IllegalArgumentException(
					"a valid point certificate requires finite numerical evidence");
		}
	}

	private boolean hasFiniteValidPayload() {
		if (!point.orElseThrow().isFinite() || rank != 3
				|| singularValues.length < 3
				|| !Double.isFinite(conditionNumber)
				|| conditionNumber < 1
				|| !Double.isFinite(maximumIntrinsicNormalizedResidual)
				|| !Double.isFinite(maximumDiagramNormalizedResidual)
				|| maximumIntrinsicNormalizedResidual < 0
				|| maximumIntrinsicNormalizedResidual > 1
				|| maximumDiagramNormalizedResidual < 0
				|| maximumDiagramNormalizedResidual > 1
				|| residualEvidence.isEmpty()) {
			return false;
		}
		for (double singularValue : singularValues) {
			if (!Double.isFinite(singularValue)) {
				return false;
			}
		}
		for (ProjectionResidualEvidence evidence : residualEvidence) {
			if (!evidence.getIntrinsicResidual().isFinite()
					|| !Double.isFinite(evidence.getIntrinsicResidualNorm())
					|| evidence.getIntrinsicResidualNorm() < 0
					|| !Double.isFinite(evidence.getNormalizedIntrinsicResidual())
					|| evidence.getNormalizedIntrinsicResidual() < 0
					|| evidence.getNormalizedIntrinsicResidual() > 1
					|| maximumIntrinsicNormalizedResidual
							< evidence.getNormalizedIntrinsicResidual()
					|| !evidence.getDiagramResidual().isFinite()
					|| !Double.isFinite(evidence.getDiagramResidualNorm())
					|| evidence.getDiagramResidualNorm() < 0
					|| !Double.isFinite(evidence.getNormalizedDiagramResidual())
					|| evidence.getNormalizedDiagramResidual() < 0
					|| evidence.getNormalizedDiagramResidual() > 1
					|| maximumDiagramNormalizedResidual
							< evidence.getNormalizedDiagramResidual()) {
				return false;
			}
		}
		return true;
	}

	private static void requireDistinctResidualKeys(
			List<ProjectionResidualEvidence> evidence) {
		Map<String, String> contentByKey = new LinkedHashMap<>();
		for (ProjectionResidualEvidence item : evidence) {
			ProjectionResidualEvidence current = Objects.requireNonNull(item);
			String previous = contentByKey.putIfAbsent(current.getEvidenceKey(),
					current.getEvidenceContentKey());
			if (previous != null && !previous.equals(
					current.getEvidenceContentKey())) {
				throw new IllegalArgumentException(
						"conflicting residual evidence key");
			}
		}
	}

	public SpatialCapabilityStatus getCapabilityStatus() {
		return capabilityStatus;
	}

	public ProjectionSystemCertificate getProjectionSystemCertificate() {
		return projectionSystemCertificate;
	}

	public ProjectionSystemStatus getProjectionSystemStatus() {
		return projectionSystemCertificate.getStatus();
	}

	public SpatialDefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	public SpatialCertificateStatus getCertificateStatus() {
		return certificateStatus;
	}

	/**
	 * Concise alias for integration code that treats this object as a result.
	 *
	 * @return point certificate status
	 */
	public SpatialCertificateStatus getStatus() {
		return certificateStatus;
	}

	public SpatialCurrentnessStatus getCurrentnessStatus() {
		return currentnessStatus;
	}

	public SpatialCurrentnessStatus getCurrentness() {
		return currentnessStatus;
	}

	public RepresentationFidelity getRepresentationFidelity() {
		return representationFidelity;
	}

	public NumericalEvidenceStatus getNumericalEvidenceStatus() {
		return numericalEvidenceStatus;
	}

	public CorrespondenceStatus getCorrespondenceStatus() {
		return correspondenceStatus;
	}

	public long getSourceRevision() {
		return sourceRevision;
	}

	/**
	 * Returns a payload only for a current, valid certificate.
	 *
	 * @return optional current spatial point payload
	 */
	public Optional<Vector3> getPoint() {
		return point;
	}

	/** @return whether this certificate carries a current valid point payload */
	public boolean hasPayload() {
		return point.isPresent();
	}

	public int getRank() {
		return rank;
	}

	public double[] getSingularValues() {
		return singularValues.clone();
	}

	public double getConditionNumber() {
		return conditionNumber;
	}

	public List<ProjectionResidualEvidence> getResidualEvidence() {
		return residualEvidence;
	}

	public double getMaximumIntrinsicNormalizedResidual() {
		return maximumIntrinsicNormalizedResidual;
	}

	public double getMaximumDiagramNormalizedResidual() {
		return maximumDiagramNormalizedResidual;
	}

	public String getArithmeticMethod() {
		return arithmeticMethod;
	}
}

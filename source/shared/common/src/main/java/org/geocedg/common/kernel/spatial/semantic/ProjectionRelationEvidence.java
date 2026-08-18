/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** Immutable common-diagram hinge or change-of-plane validation evidence. */
public final class ProjectionRelationEvidence {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final ProjectionRelationKind kind;
	private final ProjectionSystemStatus status;
	private final double lineOffsetResidual;
	private final double directionResidual;
	private final double supportPlaneResidual;
	private final double supportOrientationResidual;
	private final double foldSideDotProduct;
	private final double maximumNormalizedResidual;

	/**
	 * Creates relation evidence without explicit support-geometry residuals.
	 *
	 * @param kind relation kind
	 * @param status evaluated system status
	 * @param lineOffsetResidual common-line offset residual
	 * @param directionResidual common-line direction residual
	 * @param foldSideDotProduct fold-side dot product
	 * @param maximumNormalizedResidual maximum normalized residual
	 */
	public ProjectionRelationEvidence(ProjectionRelationKind kind,
			ProjectionSystemStatus status, double lineOffsetResidual,
			double directionResidual, double foldSideDotProduct,
			double maximumNormalizedResidual) {
		this(kind, status, lineOffsetResidual, directionResidual, Double.NaN,
				Double.NaN, foldSideDotProduct, maximumNormalizedResidual);
	}

	/** Creates keyed relation evidence without explicit support residuals. */
	public ProjectionRelationEvidence(String evidenceKey,
			ProjectionRelationKind kind, ProjectionSystemStatus status,
			double lineOffsetResidual, double directionResidual,
			double foldSideDotProduct, double maximumNormalizedResidual) {
		this(evidenceKey, kind, status, lineOffsetResidual, directionResidual,
				Double.NaN, Double.NaN, foldSideDotProduct,
				maximumNormalizedResidual);
	}

	/**
	 * Creates complete relation evidence.
	 *
	 * @param kind relation kind
	 * @param status evaluated system status
	 * @param lineOffsetResidual common-line offset residual
	 * @param directionResidual common-line direction residual
	 * @param supportPlaneResidual support-line plane-incidence residual
	 * @param supportOrientationResidual support-line orientation residual
	 * @param foldSideDotProduct fold-side dot product
	 * @param maximumNormalizedResidual maximum normalized residual
	 */
	public ProjectionRelationEvidence(ProjectionRelationKind kind,
			ProjectionSystemStatus status, double lineOffsetResidual,
			double directionResidual, double supportPlaneResidual,
			double supportOrientationResidual, double foldSideDotProduct,
			double maximumNormalizedResidual) {
		this(SemanticEvidenceKey.relationEvidence(kind, status, lineOffsetResidual,
				directionResidual, supportPlaneResidual,
				supportOrientationResidual, foldSideDotProduct,
				maximumNormalizedResidual), kind, status, lineOffsetResidual,
				directionResidual, supportPlaneResidual,
				supportOrientationResidual, foldSideDotProduct,
				maximumNormalizedResidual);
	}

	/** Creates complete keyed relation evidence. */
	public ProjectionRelationEvidence(String evidenceKey,
			ProjectionRelationKind kind, ProjectionSystemStatus status,
			double lineOffsetResidual, double directionResidual,
			double supportPlaneResidual, double supportOrientationResidual,
			double foldSideDotProduct, double maximumNormalizedResidual) {
		this.evidenceContentKey = SemanticEvidenceKey.relationEvidence(kind, status,
				lineOffsetResidual, directionResidual, supportPlaneResidual,
				supportOrientationResidual, foldSideDotProduct,
				maximumNormalizedResidual);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.kind = Objects.requireNonNull(kind);
		this.status = Objects.requireNonNull(status);
		this.lineOffsetResidual = lineOffsetResidual;
		this.directionResidual = directionResidual;
		this.supportPlaneResidual = supportPlaneResidual;
		this.supportOrientationResidual = supportOrientationResidual;
		this.foldSideDotProduct = foldSideDotProduct;
		this.maximumNormalizedResidual = maximumNormalizedResidual;
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	public ProjectionRelationKind getKind() {
		return kind;
	}

	public ProjectionSystemStatus getStatus() {
		return status;
	}

	public double getLineOffsetResidual() {
		return lineOffsetResidual;
	}

	public double getDirectionResidual() {
		return directionResidual;
	}

	public double getSupportPlaneResidual() {
		return supportPlaneResidual;
	}

	public double getSupportOrientationResidual() {
		return supportOrientationResidual;
	}

	public double getFoldSideDotProduct() {
		return foldSideDotProduct;
	}

	public double getMaximumNormalizedResidual() {
		return maximumNormalizedResidual;
	}
}

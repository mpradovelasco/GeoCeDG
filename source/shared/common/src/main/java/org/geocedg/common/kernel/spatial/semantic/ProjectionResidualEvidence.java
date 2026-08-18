/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** Paired intrinsic and composed-diagram residual evidence for one observation. */
public final class ProjectionResidualEvidence {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final int observationIndex;
	private final Vector2 intrinsicResidual;
	private final double intrinsicResidualNorm;
	private final double normalizedIntrinsicResidual;
	private final Vector2 diagramResidual;
	private final double diagramResidualNorm;
	private final double normalizedDiagramResidual;

	/**
	 * Creates intrinsic and common-diagram residual evidence for one observation.
	 *
	 * @param observationIndex zero-based observation index
	 * @param intrinsicResidual intrinsic-coordinate residual vector
	 * @param intrinsicResidualNorm intrinsic residual norm
	 * @param normalizedIntrinsicResidual normalized intrinsic residual
	 * @param diagramResidual common-diagram residual vector
	 * @param diagramResidualNorm common-diagram residual norm
	 * @param normalizedDiagramResidual normalized common-diagram residual
	 */
	public ProjectionResidualEvidence(int observationIndex, Vector2 intrinsicResidual,
			double intrinsicResidualNorm, double normalizedIntrinsicResidual,
			Vector2 diagramResidual, double diagramResidualNorm,
			double normalizedDiagramResidual) {
		this(SemanticEvidenceKey.residual(intrinsicResidual, intrinsicResidualNorm,
				normalizedIntrinsicResidual, diagramResidual, diagramResidualNorm,
				normalizedDiagramResidual), observationIndex, intrinsicResidual,
				intrinsicResidualNorm, normalizedIntrinsicResidual, diagramResidual,
				diagramResidualNorm, normalizedDiagramResidual);
	}

	/** Creates keyed residual evidence for one canonical observation. */
	public ProjectionResidualEvidence(String evidenceKey, int observationIndex,
			Vector2 intrinsicResidual, double intrinsicResidualNorm,
			double normalizedIntrinsicResidual, Vector2 diagramResidual,
			double diagramResidualNorm, double normalizedDiagramResidual) {
		if (observationIndex < 0) {
			throw new IllegalArgumentException("observationIndex must be non-negative");
		}
		this.evidenceContentKey = SemanticEvidenceKey.residual(intrinsicResidual,
				intrinsicResidualNorm, normalizedIntrinsicResidual, diagramResidual,
				diagramResidualNorm, normalizedDiagramResidual);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.observationIndex = observationIndex;
		this.intrinsicResidual = Objects.requireNonNull(intrinsicResidual);
		this.intrinsicResidualNorm = intrinsicResidualNorm;
		this.normalizedIntrinsicResidual = normalizedIntrinsicResidual;
		this.diagramResidual = Objects.requireNonNull(diagramResidual);
		this.diagramResidualNorm = diagramResidualNorm;
		this.normalizedDiagramResidual = normalizedDiagramResidual;
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	public int getObservationIndex() {
		return observationIndex;
	}

	public Vector2 getIntrinsicResidual() {
		return intrinsicResidual;
	}

	public double getIntrinsicResidualNorm() {
		return intrinsicResidualNorm;
	}

	public double getNormalizedIntrinsicResidual() {
		return normalizedIntrinsicResidual;
	}

	public Vector2 getDiagramResidual() {
		return diagramResidual;
	}

	public double getDiagramResidualNorm() {
		return diagramResidualNorm;
	}

	public double getNormalizedDiagramResidual() {
		return normalizedDiagramResidual;
	}
}

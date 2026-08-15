/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Optional;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetEvaluationStatus;

/** Closed result of independent target residual verification. */
public final class TargetResidualEvaluation2D {
	private final TargetEvaluationStatus status;
	private final Optional<TargetResidual2D> residual;
	private final String diagnostic;

	private TargetResidualEvaluation2D(TargetEvaluationStatus status,
			Optional<TargetResidual2D> residual, String diagnostic) {
		this.status = java.util.Objects.requireNonNull(status);
		this.residual = java.util.Objects.requireNonNull(residual);
		if (diagnostic == null || diagnostic.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Residual-evaluation diagnostic is required");
		}
		this.diagnostic = diagnostic;
		if ((status == TargetEvaluationStatus.ESTABLISHED)
				!= residual.isPresent()) {
			throw new IllegalArgumentException(
					"Established residual state and evidence must agree");
		}
	}

	/** @return established independent residual evidence */
	public static TargetResidualEvaluation2D established(
			TargetResidual2D residual, String diagnostic) {
		return new TargetResidualEvaluation2D(
				TargetEvaluationStatus.ESTABLISHED,
				Optional.of(java.util.Objects.requireNonNull(residual)), diagnostic);
	}

	/** @return explicit local absence of verification capability */
	public static TargetResidualEvaluation2D unavailable(
			TargetEvaluationStatus status, String diagnostic) {
		if (status == TargetEvaluationStatus.ESTABLISHED) {
			throw new IllegalArgumentException(
					"Use established() for residual evidence");
		}
		return new TargetResidualEvaluation2D(status, Optional.empty(),
				diagnostic);
	}

	public TargetEvaluationStatus getStatus() {
		return status;
	}

	public Optional<TargetResidual2D> getResidual() {
		return residual;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	public boolean isEstablished() {
		return status == TargetEvaluationStatus.ESTABLISHED;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetEvaluationStatus;

/** Typed candidate-level scalar; it is never verification evidence. */
public final class TargetCandidateEvaluation2D {
	private final TargetEvaluationStatus status;
	private final OptionalDouble level;
	private final OptionalDouble characteristicScale;
	private final String diagnostic;

	private TargetCandidateEvaluation2D(TargetEvaluationStatus status,
			OptionalDouble level, OptionalDouble characteristicScale,
			String diagnostic) {
		this.status = java.util.Objects.requireNonNull(status);
		this.level = java.util.Objects.requireNonNull(level);
		this.characteristicScale =
				java.util.Objects.requireNonNull(characteristicScale);
		if (diagnostic == null || diagnostic.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Candidate-level diagnostic is required");
		}
		this.diagnostic = diagnostic;
		if ((status == TargetEvaluationStatus.ESTABLISHED)
				!= (level.isPresent() && characteristicScale.isPresent())) {
			throw new IllegalArgumentException(
					"Established candidate state requires finite scalar evidence");
		}
		if (level.isPresent() && (!Double.isFinite(level.getAsDouble())
				|| !Double.isFinite(characteristicScale.getAsDouble())
				|| characteristicScale.getAsDouble() <= 0)) {
			throw new IllegalArgumentException(
					"Candidate scalar evidence must be finite and scaled");
		}
	}

	/** @return established candidate scalar with explicit scale */
	public static TargetCandidateEvaluation2D established(double level,
			double characteristicScale, String diagnostic) {
		return new TargetCandidateEvaluation2D(
				TargetEvaluationStatus.ESTABLISHED, OptionalDouble.of(level),
				OptionalDouble.of(characteristicScale), diagnostic);
	}

	/** @return explicit local absence of candidate capability */
	public static TargetCandidateEvaluation2D unavailable(
			TargetEvaluationStatus status, String diagnostic) {
		if (status == TargetEvaluationStatus.ESTABLISHED) {
			throw new IllegalArgumentException(
					"Use established() for candidate scalar evidence");
		}
		return new TargetCandidateEvaluation2D(status, OptionalDouble.empty(),
				OptionalDouble.empty(), diagnostic);
	}

	public TargetEvaluationStatus getStatus() {
		return status;
	}

	public OptionalDouble getLevel() {
		return level;
	}

	public OptionalDouble getCharacteristicScale() {
		return characteristicScale;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	public boolean isEstablished() {
		return status == TargetEvaluationStatus.ESTABLISHED;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;

/** Immutable typed result of one semantic point evaluation. */
public final class LocusEvaluation2D {
	private final EvaluationStatus status;
	private final LocusPoint2D point;
	private final Regularity regularity;
	private final LocusQuality2D quality;
	private final String diagnostic;

	private LocusEvaluation2D(EvaluationStatus status, LocusPoint2D point,
			Regularity regularity, LocusQuality2D quality, String diagnostic) {
		this.status = Objects.requireNonNull(status);
		this.point = point;
		this.regularity = Objects.requireNonNull(regularity);
		this.quality = Objects.requireNonNull(quality);
		this.diagnostic = diagnostic == null ? "" : diagnostic;
		if ((status == EvaluationStatus.VALID) != (point != null)) {
			throw new IllegalArgumentException("Only valid evaluations carry a point");
		}
	}

	/**
	 * Creates a valid semantic evaluation.
	 *
	 * @return immutable valid result
	 */
	public static LocusEvaluation2D valid(LocusPoint2D point,
			Regularity regularity, LocusQuality2D quality) {
		return new LocusEvaluation2D(EvaluationStatus.VALID,
				Objects.requireNonNull(point), regularity, quality, "");
	}

	/**
	 * Creates a typed evaluation without stale coordinates.
	 *
	 * @return immutable invalid result
	 */
	public static LocusEvaluation2D invalid(EvaluationStatus status,
			LocusQuality2D quality, String diagnostic) {
		if (status == EvaluationStatus.VALID) {
			throw new IllegalArgumentException("Use valid() for a valid result");
		}
		return new LocusEvaluation2D(status, null, Regularity.UNKNOWN, quality,
				diagnostic);
	}

	public EvaluationStatus getStatus() {
		return status;
	}

	public boolean isValid() {
		return status == EvaluationStatus.VALID;
	}

	public LocusPoint2D getPoint() {
		return point;
	}

	public Regularity getRegularity() {
		return regularity;
	}

	public LocusQuality2D getQuality() {
		return quality;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusEvaluation2D)) {
			return false;
		}
		LocusEvaluation2D evaluation = (LocusEvaluation2D) other;
		return status == evaluation.status && Objects.equals(point, evaluation.point)
				&& regularity == evaluation.regularity
				&& quality.equals(evaluation.quality)
				&& diagnostic.equals(evaluation.diagnostic);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, point, regularity, quality, diagnostic);
	}
}

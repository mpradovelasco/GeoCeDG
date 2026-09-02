/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;

/** One semantic preimage candidate and its local numerical evidence. */
public final class LocusPointInteractionCandidate2D {
	private final LocusSemanticAddress2D address;
	private final long sourceRevision;
	private final LocusPoint2D evaluatedPoint;
	private final double worldDistance;
	private final double intervalLower;
	private final double intervalUpper;
	private final Regularity regularity;
	private final NumericGuarantee numericGuarantee;
	private final String method;
	private final LocusPointInteractionLocalEvidence2D localEvidence;

	/** Creates one immutable package-owned candidate with explicit evidence. */
	LocusPointInteractionCandidate2D(LocusSemanticAddress2D address,
			long sourceRevision, LocusPoint2D evaluatedPoint, double worldDistance,
			double intervalLower, double intervalUpper, Regularity regularity,
			NumericGuarantee numericGuarantee, String method,
			LocusPointInteractionLocalEvidence2D localEvidence) {
		if (sourceRevision < 1 || !Double.isFinite(worldDistance)
				|| worldDistance < 0 || !Double.isFinite(intervalLower)
				|| !Double.isFinite(intervalUpper) || intervalLower > intervalUpper
				|| method == null || method.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid semantic preimage evidence");
		}
		this.address = Objects.requireNonNull(address);
		this.sourceRevision = sourceRevision;
		this.evaluatedPoint = Objects.requireNonNull(evaluatedPoint);
		this.worldDistance = worldDistance;
		this.intervalLower = intervalLower;
		this.intervalUpper = intervalUpper;
		this.regularity = Objects.requireNonNull(regularity);
		this.numericGuarantee = Objects.requireNonNull(numericGuarantee);
		this.method = method;
		this.localEvidence = Objects.requireNonNull(localEvidence);
		if (!localEvidence.isEstablished()) {
			throw new IllegalArgumentException(
					"Published candidates need established local evidence");
		}
	}

	public LocusSemanticAddress2D getAddress() {
		return address;
	}

	public long getSourceRevision() {
		return sourceRevision;
	}

	public LocusPoint2D getEvaluatedPoint() {
		return evaluatedPoint;
	}

	public double getWorldDistance() {
		return worldDistance;
	}

	public double getIntervalLower() {
		return intervalLower;
	}

	public double getIntervalUpper() {
		return intervalUpper;
	}

	public Regularity getRegularity() {
		return regularity;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public String getMethod() {
		return method;
	}

	public LocusPointInteractionLocalEvidence2D getLocalEvidence() {
		return localEvidence;
	}

	/** @return concise local-evidence diagnostic */
	public String getDiagnostic() {
		return "Forward-verified semantic minimum via " + method + ": "
				+ localEvidence.getDiagnostic();
	}
}

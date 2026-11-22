/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairCoverageStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairIsolationMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairUniquenessStatus;

/** Typed basis for local uniqueness in one semantic parameter rectangle. */
public final class LocalPairIsolationEvidence2D {
	private final LocalIsolationStatus status;
	private final PairIsolationMethod method;
	private final PairCoverageStatus coverage;
	private final PairUniquenessStatus uniqueness;
	private final NumericGuarantee numericGuarantee;
	private final String diagnostic;

	/** Creates local pair evidence and enforces the approved implication. */
	public LocalPairIsolationEvidence2D(LocalIsolationStatus status,
			PairIsolationMethod method, PairCoverageStatus coverage,
			PairUniquenessStatus uniqueness,
			NumericGuarantee numericGuarantee, String diagnostic) {
		this.status = java.util.Objects.requireNonNull(status);
		this.method = java.util.Objects.requireNonNull(method);
		this.coverage = java.util.Objects.requireNonNull(coverage);
		this.uniqueness = java.util.Objects.requireNonNull(uniqueness);
		this.numericGuarantee = java.util.Objects.requireNonNull(
				numericGuarantee);
		this.diagnostic = requireText(diagnostic);
		if (status == LocalIsolationStatus.ESTABLISHED
				&& (coverage == PairCoverageStatus.NOT_ESTABLISHED
						|| uniqueness == PairUniquenessStatus.NOT_ESTABLISHED
						|| method == PairIsolationMethod.NOT_ESTABLISHED)) {
			throw new IllegalArgumentException(
					"Pair isolation needs rectangle coverage and uniqueness");
		}
		// The numerical evidence of a regular Jacobian never borrows the words of a
		// certified rectangle, and a certified rectangle never borrows its words.
		boolean sampled = coverage == PairCoverageStatus.SAMPLED_RECTANGLE_UNDER_HYPOTHESES;
		if (sampled != (method == PairIsolationMethod.SAFEGUARDED_REGULAR_JACOBIAN)
				|| sampled != (uniqueness == PairUniquenessStatus.REGULAR_JACOBIAN_UNIQUE)
				|| sampled && numericGuarantee != NumericGuarantee.FLOATING_POINT_UNCERTIFIED) {
			throw new IllegalArgumentException(
					"A sampled rectangle is the uncertified evidence of a regular Jacobian");
		}
	}

	/**
	 * @return whether the isolation is established by a certificate or an analytic proof of
	 *         the whole rectangle; the numerical evidence of a regular Jacobian under the
	 *         hypotheses of a conditioned region is established and is not certified
	 */
	public boolean isCertified() {
		return status == LocalIsolationStatus.ESTABLISHED
				&& coverage == PairCoverageStatus.EXHAUSTIVE_RECTANGLE;
	}

	/** @return conservative unestablished local evidence */
	public static LocalPairIsolationEvidence2D notEstablished(String reason) {
		return new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.NOT_ESTABLISHED,
				PairIsolationMethod.NOT_ESTABLISHED,
				PairCoverageStatus.NOT_ESTABLISHED,
				PairUniquenessStatus.NOT_ESTABLISHED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, reason);
	}

	public LocalIsolationStatus getStatus() {
		return status;
	}

	public PairIsolationMethod getMethod() {
		return method;
	}

	public PairCoverageStatus getCoverage() {
		return coverage;
	}

	public PairUniquenessStatus getUniqueness() {
		return uniqueness;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	private static String requireText(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Isolation diagnostic is required");
		}
		return value;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/** Immutable error evidence reusing the normative G6 numerical guarantee. */
public final class MetricErrorEvidence2D {
	private final Optional<NumericGuarantee> numericGuarantee;
	private final MetricErrorAmount2D absoluteEvidence;
	private final MetricErrorAmount2D relativeEvidence;
	private final MetricErrorEvidenceScope scope;
	private final String method;
	private final List<String> assumptions;
	private final Optional<String> certificateMetadata;

	/** Creates structurally consistent metric error evidence. */
	public MetricErrorEvidence2D(Optional<NumericGuarantee> numericGuarantee,
			MetricErrorAmount2D absoluteEvidence,
			MetricErrorAmount2D relativeEvidence, MetricErrorEvidenceScope scope,
			String method, List<String> assumptions,
			Optional<String> certificateMetadata) {
		this.numericGuarantee = Objects.requireNonNull(numericGuarantee);
		this.absoluteEvidence = Objects.requireNonNull(absoluteEvidence);
		this.relativeEvidence = Objects.requireNonNull(relativeEvidence);
		this.scope = Objects.requireNonNull(scope);
		if (method == null || method.trim().isEmpty()) {
			throw new IllegalArgumentException("An error-evidence method is required");
		}
		this.method = method;
		this.assumptions = immutableStrings(assumptions);
		this.certificateMetadata = Objects.requireNonNull(certificateMetadata);
		validateShape();
	}

	/**
	 * Exact zero error under exact arithmetic.
	 *
	 * @return exact error evidence
	 */
	public static MetricErrorEvidence2D exact(String method) {
		return established(NumericGuarantee.EXACT_ARITHMETIC, 0, 0, method,
				Collections.emptyList(), Optional.empty(),
				MetricErrorEvidenceScope.COMPLETE_VALUE);
	}

	/**
	 * Certified finite evidence.
	 *
	 * @return certified error evidence
	 */
	public static MetricErrorEvidence2D certified(double absolute, double relative,
			String method, String certificate) {
		return established(NumericGuarantee.CERTIFIED_ERROR_BOUND, absolute,
				relative, method, Collections.emptyList(),
				Optional.of(Objects.requireNonNull(certificate)),
				MetricErrorEvidenceScope.COMPLETE_VALUE);
	}

	/**
	 * Estimated finite evidence with explicit assumptions.
	 *
	 * @return estimated error evidence
	 */
	public static MetricErrorEvidence2D estimated(double absolute, double relative,
			String method, List<String> assumptions) {
		if (assumptions == null || assumptions.isEmpty()) {
			throw new IllegalArgumentException(
					"Estimated error evidence needs explicit assumptions");
		}
		return established(NumericGuarantee.ESTIMATED_ERROR, absolute, relative,
				method, assumptions, Optional.empty(),
				MetricErrorEvidenceScope.COMPLETE_VALUE);
	}

	/**
	 * Uncertified evidence with no defensible amount.
	 *
	 * @return floating-point-uncertified error evidence
	 */
	public static MetricErrorEvidence2D uncertified(String method) {
		return new MetricErrorEvidence2D(
				Optional.of(NumericGuarantee.FLOATING_POINT_UNCERTIFIED),
				new NotEstablishedMetricErrorAmount2D(),
				new NotEstablishedMetricErrorAmount2D(),
				MetricErrorEvidenceScope.COMPLETE_VALUE, method,
				Collections.emptyList(), Optional.empty());
	}

	/**
	 * Error evidence does not apply to an absent or infinite value.
	 *
	 * @return structurally non-applicable error evidence
	 */
	public static MetricErrorEvidence2D notApplicable(String method) {
		return new MetricErrorEvidence2D(Optional.empty(),
				new NotApplicableMetricErrorAmount2D(),
				new NotApplicableMetricErrorAmount2D(),
				MetricErrorEvidenceScope.NOT_APPLICABLE, method,
				Collections.emptyList(), Optional.empty());
	}

	/**
	 * Established evidence with an explicit aggregate scope.
	 *
	 * @return scoped established error evidence
	 */
	public static MetricErrorEvidence2D established(NumericGuarantee guarantee,
			double absolute, double relative, String method,
			List<String> assumptions, Optional<String> certificate,
			MetricErrorEvidenceScope scope) {
		return new MetricErrorEvidence2D(Optional.of(guarantee),
				new EstablishedMetricErrorAmount2D(absolute),
				new EstablishedMetricErrorAmount2D(relative), scope, method,
				assumptions, certificate);
	}

	public Optional<NumericGuarantee> getNumericGuarantee() {
		return numericGuarantee;
	}

	public MetricErrorAmount2D getAbsoluteEvidence() {
		return absoluteEvidence;
	}

	public MetricErrorAmount2D getRelativeEvidence() {
		return relativeEvidence;
	}

	public MetricErrorEvidenceScope getScope() {
		return scope;
	}

	public String getMethod() {
		return method;
	}

	public List<String> getAssumptions() {
		return assumptions;
	}

	public Optional<String> getCertificateMetadata() {
		return certificateMetadata;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricErrorEvidence2D)) {
			return false;
		}
		MetricErrorEvidence2D evidence = (MetricErrorEvidence2D) other;
		return numericGuarantee.equals(evidence.numericGuarantee)
				&& absoluteEvidence.equals(evidence.absoluteEvidence)
				&& relativeEvidence.equals(evidence.relativeEvidence)
				&& scope == evidence.scope && method.equals(evidence.method)
				&& assumptions.equals(evidence.assumptions)
				&& certificateMetadata.equals(evidence.certificateMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numericGuarantee, absoluteEvidence, relativeEvidence,
				scope, method, assumptions, certificateMetadata);
	}

	private void validateShape() {
		boolean notApplicable = scope == MetricErrorEvidenceScope.NOT_APPLICABLE;
		if (notApplicable != numericGuarantee.isEmpty()
				|| notApplicable != (absoluteEvidence.getKind()
						== MetricErrorAmountKind.NOT_APPLICABLE)
				|| notApplicable != (relativeEvidence.getKind()
						== MetricErrorAmountKind.NOT_APPLICABLE)) {
			throw new IllegalArgumentException(
					"Error guarantee, amounts and scope must agree structurally");
		}
		if (numericGuarantee.orElse(null) == NumericGuarantee.ESTIMATED_ERROR
				&& assumptions.isEmpty()) {
			throw new IllegalArgumentException(
					"Estimated evidence requires explicit assumptions");
		}
	}

	private static List<String> immutableStrings(List<String> input) {
		Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException(
						"Error-evidence assumptions must be non-empty");
			}
			copy.add(value);
		}
		return Collections.unmodifiableList(copy);
	}
}

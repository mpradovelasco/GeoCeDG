/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;

/** Immutable evaluator of {@code T(L(u))} over one coherent source revision. */
public final class LocusSimilarityEvaluator2D implements LocusEvaluator2D,
		LocusDifferentialEvaluator2D, PiecewisePolynomialLocus2D {
	private final LocusDefinition2D sourceDefinition;
	private final LocusSimilarityTransform2D transform;
	private final String signature;

	/** Captures the exact immutable source snapshot and finite transformation. */
	public LocusSimilarityEvaluator2D(LocusDefinition2D sourceDefinition,
			LocusSimilarityTransform2D transform) {
		this.sourceDefinition = Objects.requireNonNull(sourceDefinition);
		this.transform = Objects.requireNonNull(transform);
		signature = "locus-similarity-evaluator/v1|source="
				+ sourceDefinition.getLocusIdentity() + "@"
				+ sourceDefinition.getSemanticRevision() + "|"
				+ transform.getSemanticSignature();
	}

	@Override
	public LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session) {
		LocusEvaluation2D source = sourceDefinition.evaluate(branch.getBranchKey(),
				canonicalParameter, session);
		if (!source.isValid()) {
			return LocusEvaluation2D.invalid(source.getStatus(),
					transformedQuality(source.getQuality()), source.getDiagnostic());
		}
		try {
			return LocusEvaluation2D.valid(transform.transform(source.getPoint()),
					transform.isCollapsed() ? Regularity.SINGULAR
							: source.getRegularity(),
					transformedQuality(source.getQuality()));
		} catch (IllegalArgumentException exception) {
			return LocusEvaluation2D.invalid(EvaluationStatus.NON_FINITE,
					transformedQuality(source.getQuality()),
					"Similarity image is nonfinite");
		}
	}

	@Override
	public boolean supportsDifferential(LocusDefinition2D definition) {
		return sourceDefinition.getEvaluatorCapability()
				instanceof LocusDifferentialEvaluator2D
				&& ((LocusDifferentialEvaluator2D) sourceDefinition
						.getEvaluatorCapability())
						.supportsDifferential(sourceDefinition);
	}

	@Override
	public LocusDifferentialEvaluation2D evaluateDifferential(
			LocusDefinition2D definition, String branchKey,
			double providerCanonicalParameter,
			LocusEvaluationSession2D session) {
		if (!supportsDifferential(definition)) {
			return LocusDifferentialEvaluation2D.invalid(
					"Source locus has no semantic derivative capability");
		}
		LocusDifferentialEvaluation2D source =
				((LocusDifferentialEvaluator2D) sourceDefinition
						.getEvaluatorCapability()).evaluateDifferential(
							sourceDefinition, branchKey,
							providerCanonicalParameter, session);
		if (source == null || !source.isValid()) {
			return LocusDifferentialEvaluation2D.invalid(source == null
					? "Source differential evaluator returned null"
					: source.getDiagnostic());
		}
		LocusPoint2D derivative = transform.transformDerivative(
				source.getDerivativeX(), source.getDerivativeY());
		return LocusDifferentialEvaluation2D.valid(derivative.getX(),
				derivative.getY());
	}

	@Override
	public List<Double> getInteriorBreakpoints(String branchKey, double lower,
			double upper) {
		if (sourceDefinition.getEvaluatorCapability()
				instanceof LocusParameterPartition2D) {
			return ((LocusParameterPartition2D) sourceDefinition
					.getEvaluatorCapability()).getInteriorBreakpoints(branchKey,
						lower, upper);
		}
		return Collections.emptyList();
	}

	@Override
	public boolean supportsPiecewisePolynomial(LocusDefinition2D definition) {
		return sourceDefinition.getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D
				&& ((PiecewisePolynomialLocus2D) sourceDefinition
						.getEvaluatorCapability())
						.supportsPiecewisePolynomial(sourceDefinition);
	}

	@Override
	public int getPolynomialSpanCount(String branchKey) {
		return polynomialSource().getPolynomialSpanCount(branchKey);
	}

	@Override
	public double getPolynomialSpanLower(String branchKey, int spanIndex) {
		return polynomialSource().getPolynomialSpanLower(branchKey, spanIndex);
	}

	@Override
	public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
		return polynomialSource().getPolynomialSpanUpper(branchKey, spanIndex);
	}

	@Override
	public double[] getPolynomialCoefficients(String branchKey, int spanIndex,
			int coordinate) {
		PiecewisePolynomialLocus2D source = polynomialSource();
		double[][] transformed = transform.transformPolynomialCoefficients(
				source.getPolynomialCoefficients(branchKey, spanIndex, 0),
				source.getPolynomialCoefficients(branchKey, spanIndex, 1));
		if (coordinate < 0 || coordinate > 1) {
			throw new IllegalArgumentException("Polynomial coordinate must be x or y");
		}
		return transformed[coordinate];
	}

	@Override
	public LocusPoint2D evaluatePolynomialDerivative(String branchKey,
			double providerCanonicalParameter) {
		LocusPoint2D source = polynomialSource()
				.evaluatePolynomialDerivative(branchKey,
						providerCanonicalParameter);
		return transform.transformDerivative(source.getX(), source.getY());
	}

	@Override
	public String getPolynomialCapabilitySignature() {
		return "locus-similarity-piecewise-polynomial/v1|source="
				+ polynomialSource().getPolynomialCapabilitySignature() + "|"
				+ transform.getSemanticSignature();
	}

	private PiecewisePolynomialLocus2D polynomialSource() {
		if (!(sourceDefinition.getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D)) {
			throw new IllegalStateException(
					"Source locus has no piecewise-polynomial capability");
		}
		return (PiecewisePolynomialLocus2D) sourceDefinition
				.getEvaluatorCapability();
	}

	/** @return stable signature of the captured source revision and transform */
	public String getEvaluatorSignature() {
		return signature;
	}

	private static LocusQuality2D transformedQuality(LocusQuality2D source) {
		return new LocusQuality2D(source.getConstructionFidelity(),
				source.getEvaluationMethod(), source.getRepresentationRole(),
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;

/** Immutable evaluator of {@code T(L(u))} over one coherent source revision. */
public final class LocusSimilarityEvaluator2D implements LocusEvaluator2D,
		LocusDifferentialEvaluator2D, PiecewisePolynomialLocus2D,
		CertifiedAffineLocus2D {
	private final LocusDefinition2D sourceDefinition;
	private final LocusSimilarityTransform2D transform;
	private final String signature;
	private final Map<String, double[][]> certifiedAffineByBranch;
	private final int compositionDepth;

	/** Captures the exact immutable source snapshot and finite transformation. */
	public LocusSimilarityEvaluator2D(LocusDefinition2D sourceDefinition,
			LocusSimilarityTransform2D transform) {
		this.sourceDefinition = Objects.requireNonNull(sourceDefinition);
		this.transform = Objects.requireNonNull(transform);
		Object sourceCapability = sourceDefinition.getEvaluatorCapability();
		boolean polynomialSource = sourceCapability
				instanceof PiecewisePolynomialLocus2D;
		int sourceDepth = polynomialSource
				? ((PiecewisePolynomialLocus2D) sourceCapability)
						.getPolynomialCompositionDepth() : 0;
		compositionDepth = (polynomialSource && sourceDepth < 1)
				|| sourceDepth == Integer.MAX_VALUE
				? Integer.MAX_VALUE : sourceDepth + 1;
		certifiedAffineByBranch = captureCertifiedAffine(sourceCapability);
		signature = "locus-similarity-evaluator/v1|source="
				+ sourceDefinition.getLocusIdentity() + "@"
				+ sourceDefinition.getSemanticRevision() + "|"
				+ transform.getSemanticSignature();
	}

	@Override
	public boolean supportsCertifiedAffine(LocusDefinition2D definition) {
		return !certifiedAffineByBranch.isEmpty();
	}

	@Override
	public double[] getCertifiedAffineCoefficients(String branchKey,
			int coordinate) {
		double[][] coefficients = certifiedAffineByBranch.get(branchKey);
		if (coefficients == null || coordinate < 0 || coordinate > 1) {
			throw new IllegalArgumentException(
					"No certified affine coefficients for the requested branch");
		}
		return coefficients[coordinate].clone();
	}

	@Override
	public String getCertifiedAffineSignature() {
		StringBuilder value = new StringBuilder(
				"similarity-certified-affine/v1");
		for (Map.Entry<String, double[][]> entry
				: certifiedAffineByBranch.entrySet()) {
			double[][] coefficients = entry.getValue();
			value.append('|').append(entry.getKey()).append(':')
					.append(Double.toHexString(coefficients[0][0])).append(',')
					.append(Double.toHexString(coefficients[0][1])).append(';')
					.append(Double.toHexString(coefficients[1][0])).append(',')
					.append(Double.toHexString(coefficients[1][1]));
		}
		return value.toString();
	}

	/** @return number of captured similarity evaluators in this normal-DAG chain */
	public int getCompositionDepth() {
		return compositionDepth;
	}

	@Override
	public int getPolynomialCompositionDepth() {
		return compositionDepth;
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
		return compositionDepth <= MAXIMUM_SAFE_COMPOSITION_DEPTH
				&& sourceDefinition.getEvaluatorCapability()
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
		if (compositionDepth > MAXIMUM_SAFE_COMPOSITION_DEPTH) {
			return Collections.emptyList();
		}
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
		return compositionDepth <= MAXIMUM_SAFE_COMPOSITION_DEPTH
				&& sourceDefinition.getEvaluatorCapability()
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
		if (coordinate < 0 || coordinate > 1) {
			throw new IllegalArgumentException("Polynomial coordinate must be x or y");
		}
		return getPolynomialCoordinateCoefficients(branchKey, spanIndex)[coordinate];
	}

	@Override
	public double[][] getPolynomialCoordinateCoefficients(String branchKey,
			int spanIndex) {
		double[][] source = polynomialSource()
				.getPolynomialCoordinateCoefficients(branchKey, spanIndex);
		if (source == null || source.length != 2) {
			throw new IllegalStateException(
					"Source polynomial capability returned no coordinate pair");
		}
		return transform.transformPolynomialCoefficients(source[0], source[1]);
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
		if (compositionDepth > MAXIMUM_SAFE_COMPOSITION_DEPTH) {
			throw new IllegalStateException(
					"Polynomial similarity composition exceeds the safe depth");
		}
		if (!(sourceDefinition.getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D)) {
			throw new IllegalStateException(
					"Source locus has no piecewise-polynomial capability");
		}
		return (PiecewisePolynomialLocus2D) sourceDefinition
				.getEvaluatorCapability();
	}

	private Map<String, double[][]> captureCertifiedAffine(
			Object sourceCapability) {
		if (!(sourceCapability instanceof CertifiedAffineLocus2D)
				|| !((CertifiedAffineLocus2D) sourceCapability)
						.supportsCertifiedAffine(sourceDefinition)) {
			return Collections.emptyMap();
		}
		CertifiedAffineLocus2D affine = (CertifiedAffineLocus2D) sourceCapability;
		LinkedHashMap<String, double[][]> captured = new LinkedHashMap<>();
		try {
			for (LocusBranch2D branch : sourceDefinition.getBranches()) {
				double[] x = affine.getCertifiedAffineCoefficients(
						branch.getBranchKey(), 0);
				double[] y = affine.getCertifiedAffineCoefficients(
						branch.getBranchKey(), 1);
				if (x == null || y == null || x.length != 2 || y.length != 2) {
					return Collections.emptyMap();
				}
				LocusPoint2D slope = transform.transformDerivative(x[0], y[0]);
				LocusPoint2D intercept = transform.transform(
						new LocusPoint2D(x[1], y[1]));
				captured.put(branch.getBranchKey(), new double[][] {
						{slope.getX(), intercept.getX()},
						{slope.getY(), intercept.getY()}});
			}
		} catch (IllegalArgumentException nonfiniteCertificate) {
			// The affine certificate is an optional acceleration/certification
			// capability. A finite transform may still map a finite coefficient
			// representation beyond double range; ordinary semantic evaluation
			// must remain available and report NON_FINITE at the queried address.
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(captured);
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

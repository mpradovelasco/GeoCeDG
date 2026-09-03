/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusParameterPartition2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;

/** Floating evaluation of one immutable structurally continuous SplineV2 revision. */
public final class SplineSemanticEvaluator2D implements LocusEvaluator2D,
		LocusDifferentialEvaluator2D, PiecewisePolynomialLocus2D {
	private static final LocusQuality2D QUALITY = new LocusQuality2D(
			ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
			EvaluationMethod.ANALYTIC_EVALUATION,
			RepresentationRole.SEMANTIC_RESULT,
			NumericGuarantee.FLOATING_POINT_UNCERTIFIED);

	private final String branchKey;
	private final SplinePolynomialModel2D model;

	/** Captures one immutable polynomial model and stable semantic branch. */
	public SplineSemanticEvaluator2D(String branchKey,
			SplinePolynomialModel2D model) {
		if (branchKey == null || branchKey.trim().isEmpty()) {
			throw new IllegalArgumentException("SplineV2 branch key is required");
		}
		this.branchKey = branchKey;
		this.model = Objects.requireNonNull(model);
	}

	@Override
	public LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session) {
		try {
			double[] point = model.evaluate(canonicalParameter);
			double[] derivative = model.evaluateDerivative(canonicalParameter);
			double speed = Math.hypot(derivative[0], derivative[1]);
			if (!Double.isFinite(point[0]) || !Double.isFinite(point[1])
					|| !Double.isFinite(speed)) {
				return LocusEvaluation2D.invalid(
						org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
								.EvaluationStatus.NON_FINITE,
						QUALITY, "SplineV2 polynomial evaluation is nonfinite");
			}
			return LocusEvaluation2D.valid(new LocusPoint2D(point[0], point[1]),
					speed == 0 ? Regularity.SINGULAR
							: Regularity.REGULAR,
					QUALITY);
		} catch (IllegalArgumentException exception) {
			return LocusEvaluation2D.invalid(
					org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
							.EvaluationStatus.OUT_OF_DOMAIN,
					QUALITY, exception.getMessage());
		}
	}

	@Override
	public LocusDifferentialEvaluation2D evaluateDifferential(
			LocusDefinition2D definition, String requestedBranchKey,
			double canonicalParameter, LocusEvaluationSession2D session) {
		if (!branchKey.equals(requestedBranchKey)) {
			return LocusDifferentialEvaluation2D.invalid(
					"Unknown SplineV2 semantic branch");
		}
		try {
			double[] derivative = model.evaluateDerivative(canonicalParameter);
			if (!Double.isFinite(derivative[0])
					|| !Double.isFinite(derivative[1])) {
				return LocusDifferentialEvaluation2D.invalid(
						"SplineV2 derivative is nonfinite");
			}
			return LocusDifferentialEvaluation2D.valid(derivative[0],
					derivative[1]);
		} catch (IllegalArgumentException exception) {
			return LocusDifferentialEvaluation2D.invalid(exception.getMessage());
		}
	}

	@Override
	public List<Double> getInteriorBreakpoints(String requestedBranchKey,
			double lower, double upper) {
		if (!branchKey.equals(requestedBranchKey) || !Double.isFinite(lower)
				|| !Double.isFinite(upper)) {
			return Collections.emptyList();
		}
		double minimum = Math.min(lower, upper);
		double maximum = Math.max(lower, upper);
		ArrayList<Double> breakpoints = new ArrayList<>();
		double[] knots = model.getKnots();
		for (int index = 1; index + 1 < knots.length; index++) {
			if (knots[index] > minimum && knots[index] < maximum) {
				breakpoints.add(knots[index]);
			}
		}
		return Collections.unmodifiableList(breakpoints);
	}

	@Override
	public int getPolynomialSpanCount(String requestedBranchKey) {
		requireBranch(requestedBranchKey);
		return model.getSpanCount();
	}

	@Override
	public double getPolynomialSpanLower(String requestedBranchKey,
			int spanIndex) {
		requireSpan(requestedBranchKey, spanIndex);
		return model.getKnots()[spanIndex];
	}

	@Override
	public double getPolynomialSpanUpper(String requestedBranchKey,
			int spanIndex) {
		requireSpan(requestedBranchKey, spanIndex);
		return model.getKnots()[spanIndex + 1];
	}

	@Override
	public double[] getPolynomialCoefficients(String requestedBranchKey,
			int spanIndex, int coordinate) {
		requireSpan(requestedBranchKey, spanIndex);
		if (coordinate < 0 || coordinate > 1) {
			throw new IllegalArgumentException("SplineV2 coordinate must be x or y");
		}
		return model.getCoefficients(spanIndex, coordinate);
	}

	@Override
	public LocusPoint2D evaluatePolynomialDerivative(String requestedBranchKey,
			double providerCanonicalParameter) {
		requireBranch(requestedBranchKey);
		double[] derivative = model.evaluateDerivative(
				providerCanonicalParameter);
		if (!Double.isFinite(derivative[0])
				|| !Double.isFinite(derivative[1])) {
			throw new IllegalArgumentException(
					"SplineV2 polynomial derivative is nonfinite");
		}
		return new LocusPoint2D(derivative[0], derivative[1]);
	}

	@Override
	public String getPolynomialCapabilitySignature() {
		return "spline-v2-piecewise-polynomial/v2|"
				+ model.getSemanticSignature();
	}

	private void requireBranch(String requestedBranchKey) {
		if (!branchKey.equals(requestedBranchKey)) {
			throw new IllegalArgumentException("Unknown SplineV2 semantic branch");
		}
	}

	private void requireSpan(String requestedBranchKey, int spanIndex) {
		requireBranch(requestedBranchKey);
		if (spanIndex < 0 || spanIndex >= model.getSpanCount()) {
			throw new IllegalArgumentException("Unknown SplineV2 polynomial span");
		}
	}

	/** @return immutable polynomial authority for focused kernel consumers */
	public SplinePolynomialModel2D getModel() {
		return model;
	}

	/** @return deterministic evaluator signature */
	public String getEvaluatorSignature() {
		return "spline-v2-evaluator/v2|branch=" + branchKey + "|"
				+ model.getSemanticSignature();
	}
}

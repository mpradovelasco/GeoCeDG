/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocalPiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;
import org.geocedg.common.math.bspline.BSplineCanonicalEncoding;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineEvaluation;
import org.geocedg.common.math.bspline.BSplineGlobalPolynomial;
import org.geocedg.common.math.bspline.BSplineInteriorKnot;
import org.geocedg.common.math.bspline.BSplineOutcome;
import org.geocedg.common.math.bspline.BSplinePoint;
import org.geocedg.common.math.bspline.BSplineSpanForms;
import org.geocedg.common.math.bspline.BSplineSpanPolynomial;

/**
 * Floating evaluation of one immutable {@code bspline-2d/v1} revision
 * ({@code bspline-2d.md} sections 9 and 11) over the shared library. Points and first
 * derivatives come from the homogeneous de Boor and hodograph algorithms; the polynomial
 * authority is the local power form of each non-degenerate span, published through
 * {@link LocalPiecewisePolynomialLocus2D}; the inherited global coefficients are the
 * library's correctly rounded expansion and carry no authority. For an exactly closed curve
 * the parameter {@code b} is identified with {@code a} (section 3.3).
 */
public final class BSplineSemanticEvaluator2D implements LocusEvaluator2D,
		LocusDifferentialEvaluator2D, LocalPiecewisePolynomialLocus2D {
	private static final LocusQuality2D QUALITY = new LocusQuality2D(
			ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
			EvaluationMethod.ANALYTIC_EVALUATION,
			RepresentationRole.SEMANTIC_RESULT,
			NumericGuarantee.FLOATING_POINT_UNCERTIFIED);

	private final String branchKey;
	private final BSplineData data;
	private final boolean closed;
	private final double[][][] local;
	private final double conditioning;
	private final String digest;

	/**
	 * @param branchKey stable semantic branch
	 * @param data validated polynomial data of the revision
	 */
	public BSplineSemanticEvaluator2D(String branchKey, BSplineData data) {
		if (branchKey == null || branchKey.trim().isEmpty()) {
			throw new IllegalArgumentException("BSpline branch key is required");
		}
		this.branchKey = branchKey;
		this.data = Objects.requireNonNull(data);
		this.closed = data.isExactlyClosed();
		int spans = data.spanCount();
		local = new double[spans][][];
		double kappa = 1;
		for (int span = 0; span < spans; span++) {
			BSplineOutcome<BSplineSpanPolynomial> form = BSplineSpanForms
					.localPowerDescending(data, data.spanKnotIndex(span));
			if (!form.isOk()) {
				throw new IllegalArgumentException("BSpline span form: " + form.status());
			}
			local[span] = new double[][] {form.value().x(), form.value().y()};
			kappa = Math.max(kappa, reexpansionAmplification(spanLower(span),
					spanUpper(span), data.degree()));
		}
		conditioning = kappa;
		digest = BSplineCanonicalEncoding.digestHex(BSplineDefinition2D.CONTRACT, data);
	}

	/**
	 * Amplification {@code ((|alpha| + h) / h)^p} of the re-expansion of a span's local
	 * polynomial in powers of the global parameter (section 11.3).
	 *
	 * @param lower span start {@code alpha}
	 * @param upper span end
	 * @param degree degree {@code p}
	 * @return amplification, at least 1, positive infinity when it overflows
	 */
	public static double reexpansionAmplification(double lower, double upper, int degree) {
		double width = upper - lower;
		return Math.pow((Math.abs(lower) + width) / width, degree);
	}

	private double canonical(double parameter) {
		return closed && parameter == data.domainEnd() ? data.domainStart() : parameter;
	}

	@Override
	public LocusEvaluation2D evaluate(LocusDefinition2D definition, LocusBranch2D branch,
			double canonicalParameter, LocusEvaluationSession2D session) {
		double parameter = canonical(canonicalParameter);
		BSplinePoint point = BSplineEvaluation.point(data, parameter);
		BSplinePoint derivative = BSplineEvaluation.firstDerivative(data, parameter);
		if (!point.isOk() || !derivative.isOk()) {
			return LocusEvaluation2D.invalid(EvaluationStatus.OUT_OF_DOMAIN, QUALITY,
					"BSpline parameter outside the domain: " + point.status());
		}
		double speed = Math.hypot(derivative.x(), derivative.y());
		if (!Double.isFinite(point.x()) || !Double.isFinite(point.y())
				|| !Double.isFinite(speed)) {
			return LocusEvaluation2D.invalid(EvaluationStatus.NON_FINITE, QUALITY,
					"BSpline evaluation is nonfinite");
		}
		return LocusEvaluation2D.valid(new LocusPoint2D(point.x(), point.y()),
				speed == 0 ? Regularity.SINGULAR : Regularity.REGULAR, QUALITY);
	}

	@Override
	public LocusDifferentialEvaluation2D evaluateDifferential(LocusDefinition2D definition,
			String requestedBranchKey, double canonicalParameter,
			LocusEvaluationSession2D session) {
		if (!branchKey.equals(requestedBranchKey)) {
			return LocusDifferentialEvaluation2D.invalid("Unknown BSpline semantic branch");
		}
		BSplinePoint derivative = BSplineEvaluation.firstDerivative(data,
				canonical(canonicalParameter));
		if (!derivative.isOk() || !Double.isFinite(derivative.x())
				|| !Double.isFinite(derivative.y())) {
			return LocusDifferentialEvaluation2D.invalid("BSpline derivative unavailable: "
					+ derivative.status());
		}
		return LocusDifferentialEvaluation2D.valid(derivative.x(), derivative.y());
	}

	@Override
	public List<Double> getInteriorBreakpoints(String requestedBranchKey, double lower,
			double upper) {
		if (!branchKey.equals(requestedBranchKey) || !Double.isFinite(lower)
				|| !Double.isFinite(upper)) {
			return Collections.emptyList();
		}
		double minimum = Math.min(lower, upper);
		double maximum = Math.max(lower, upper);
		ArrayList<Double> breakpoints = new ArrayList<>();
		for (BSplineInteriorKnot knot : data.interiorKnots()) {
			if (knot.value() > minimum && knot.value() < maximum) {
				breakpoints.add(knot.value());
			}
		}
		return Collections.unmodifiableList(breakpoints);
	}

	@Override
	public int getPolynomialSpanCount(String requestedBranchKey) {
		requireBranch(requestedBranchKey);
		return data.spanCount();
	}

	@Override
	public double getPolynomialSpanLower(String requestedBranchKey, int spanIndex) {
		requireSpan(requestedBranchKey, spanIndex);
		return spanLower(spanIndex);
	}

	@Override
	public double getPolynomialSpanUpper(String requestedBranchKey, int spanIndex) {
		requireSpan(requestedBranchKey, spanIndex);
		return spanUpper(spanIndex);
	}

	private double spanLower(int spanIndex) {
		return data.knot(data.spanKnotIndex(spanIndex));
	}

	private double spanUpper(int spanIndex) {
		return data.knot(data.spanKnotIndex(spanIndex) + 1);
	}

	/**
	 * Global coefficients for interface conformance only: the library's correctly rounded
	 * expansion of the local polynomial (section 11.3, item 3), never read by a routed
	 * consumer.
	 */
	@Override
	public double[] getPolynomialCoefficients(String requestedBranchKey, int spanIndex,
			int coordinate) {
		if (coordinate < 0 || coordinate > 1) {
			throw new IllegalArgumentException("BSpline coordinate must be x or y");
		}
		return getPolynomialCoordinateCoefficients(requestedBranchKey, spanIndex)[coordinate];
	}

	@Override
	public double[][] getPolynomialCoordinateCoefficients(String requestedBranchKey,
			int spanIndex) {
		requireSpan(requestedBranchKey, spanIndex);
		BSplineOutcome<BSplineGlobalPolynomial> global = BSplineSpanForms.globalPower(data,
				data.spanKnotIndex(spanIndex));
		if (!global.isOk()) {
			throw new IllegalArgumentException("BSpline global form: " + global.status());
		}
		return new double[][] {descending(global.value().x()),
				descending(global.value().y())};
	}

	private static double[] descending(double[] ascending) {
		double[] result = new double[ascending.length];
		for (int index = 0; index < ascending.length; index++) {
			result[index] = ascending[ascending.length - 1 - index];
		}
		return result;
	}

	@Override
	public boolean publishesLocalPolynomialCoefficients(LocusDefinition2D definition) {
		return true;
	}

	@Override
	public double[][] getLocalPolynomialCoordinateCoefficients(String requestedBranchKey,
			int spanIndex) {
		requireSpan(requestedBranchKey, spanIndex);
		return new double[][] {local[spanIndex][0].clone(), local[spanIndex][1].clone()};
	}

	@Override
	public double getGlobalReexpansionConditioning(String requestedBranchKey) {
		requireBranch(requestedBranchKey);
		return conditioning;
	}

	@Override
	public LocusPoint2D evaluatePolynomialDerivative(String requestedBranchKey,
			double providerCanonicalParameter) {
		requireBranch(requestedBranchKey);
		BSplinePoint derivative = BSplineEvaluation.firstDerivative(data,
				canonical(providerCanonicalParameter));
		if (!derivative.isOk() || !Double.isFinite(derivative.x())
				|| !Double.isFinite(derivative.y())) {
			throw new IllegalArgumentException("BSpline polynomial derivative unavailable");
		}
		return new LocusPoint2D(derivative.x(), derivative.y());
	}

	@Override
	public String getPolynomialCapabilitySignature() {
		return "bspline-2d-local-polynomial/v1|" + digest;
	}

	/** @return deterministic evaluator signature of the captured revision */
	public String getEvaluatorSignature() {
		return "bspline-2d-evaluator/v1|branch=" + branchKey + "|" + digest;
	}

	/** @return immutable library data of the revision */
	public BSplineData getData() {
		return data;
	}

	private void requireBranch(String requestedBranchKey) {
		if (!branchKey.equals(requestedBranchKey)) {
			throw new IllegalArgumentException("Unknown BSpline semantic branch");
		}
	}

	private void requireSpan(String requestedBranchKey, int spanIndex) {
		requireBranch(requestedBranchKey);
		if (spanIndex < 0 || spanIndex >= data.spanCount()) {
			throw new IllegalArgumentException("Unknown BSpline polynomial span");
		}
	}
}

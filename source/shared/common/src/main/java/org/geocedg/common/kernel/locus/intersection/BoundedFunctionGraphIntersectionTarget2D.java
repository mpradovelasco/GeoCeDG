/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MembershipStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetEvaluationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geogebra.common.kernel.arithmetic.Function;
import org.geogebra.common.kernel.geos.GeoFunction;

/** Captured vertical-residual adapter for an explicitly bounded real function. */
public final class BoundedFunctionGraphIntersectionTarget2D
		implements LocusIntersectionTarget2D {
	private static final String ADAPTER_VERSION =
			"g8c1-bounded-function-vertical-residual/v1";

	private final String targetIdentity;
	private final long targetUpdateStamp;
	private final Function function;
	private final Function derivative;
	private final IntersectionTargetDomain2D domain;
	private final IntersectionResidualContract2D contract;

	/** Captures one function expression and its explicit finite x-domain. */
	public BoundedFunctionGraphIntersectionTarget2D(GeoFunction target,
			String targetIdentity, long targetUpdateStamp) {
		java.util.Objects.requireNonNull(target);
		if (!target.isDefined() || target.isBooleanFunction()
				|| target.getFunction() == null || !target.hasInterval()
				|| !Double.isFinite(target.getIntervalMin())
				|| !Double.isFinite(target.getIntervalMax())
				|| target.getIntervalMin() > target.getIntervalMax()) {
			throw new IllegalArgumentException(
					"G8C1 function target needs a real expression and explicit finite domain");
		}
		this.targetIdentity = requireIdentity(targetIdentity);
		this.targetUpdateStamp = requireStamp(targetUpdateStamp);
		this.function = target.getFunction().deepCopy(target.getKernel());
		Function capturedDerivative = null;
		try {
			capturedDerivative = function.getDerivative(1, true);
		} catch (RuntimeException exception) {
			// Derivative capability is optional and explicitly degrades below.
		}
		this.derivative = capturedDerivative;
		this.domain = IntersectionTargetDomain2D.finiteClosedXInterval(
				target.getIntervalMin(), target.getIntervalMax(),
				"Captured GeoFunction explicit interval; never view path bounds");
		this.contract = new IntersectionResidualContract2D(ADAPTER_VERSION,
				ResidualQuantityKind.VERTICAL_MODEL_LENGTH, "model-coordinate",
				"captured GeoFunction graph vertical residual y-f(x); not Euclidean distance",
				"unit model-coordinate length");
	}

	@Override
	public TargetFamily getFamily() {
		return TargetFamily.BOUNDED_FUNCTION_GRAPH;
	}

	@Override
	public String getTargetIdentity() {
		return targetIdentity;
	}

	@Override
	public long getTargetUpdateStamp() {
		return targetUpdateStamp;
	}

	@Override
	public IntersectionResidualContract2D getResidualContract() {
		return contract;
	}

	@Override
	public IntersectionTargetDomain2D getDomainContract() {
		return domain;
	}

	@Override
	public TargetCandidateEvaluation2D evaluateCandidateLevel(
			LocusPoint2D point) {
		FunctionValue value = evaluateFunction(point);
		return value.established
				? TargetCandidateEvaluation2D.established(
						point.getY() - value.value, 1,
						"Function candidate level uses captured vertical graph residual")
				: TargetCandidateEvaluation2D.unavailable(value.status,
						value.diagnostic);
	}

	@Override
	public TargetResidual2D evaluateResidual(LocusPoint2D point) {
		TargetResidualEvaluation2D evaluation = evaluateResidualEvidence(point);
		if (!evaluation.isEstablished()) {
			throw new IllegalArgumentException(evaluation.getDiagnostic());
		}
		return evaluation.getResidual().get();
	}

	@Override
	public TargetResidualEvaluation2D evaluateResidualEvidence(
			LocusPoint2D point) {
		FunctionValue value = evaluateFunction(point);
		if (!value.established) {
			return TargetResidualEvaluation2D.unavailable(value.status,
					value.diagnostic);
		}
		double vertical = point.getY() - value.value;
		if (!Double.isFinite(vertical)) {
			return TargetResidualEvaluation2D.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Function vertical residual is nonfinite");
		}
		return TargetResidualEvaluation2D.established(
				new TargetResidual2D(vertical, 1, vertical, 1, contract),
				"Independent captured function vertical-residual verification");
	}

	@Override
	public TargetMembership2D evaluateMembership(LocusPoint2D point,
			double coordinateTolerance) {
		if (point == null || !Double.isFinite(point.getX())
				|| !domain.containsX(point.getX(), coordinateTolerance)) {
			return new TargetMembership2D(MembershipStatus.NOT_MEMBER, false,
					"Point x is outside the captured explicit function domain");
		}
		FunctionValue value = evaluateFunction(point);
		return new TargetMembership2D(value.established ? MembershipStatus.MEMBER
				: MembershipStatus.NOT_ESTABLISHED,
				domain.isIncludedBoundary(point.getX(), coordinateTolerance),
				value.established
						? "Finite function evaluation establishes graph-domain membership"
						: value.diagnostic);
	}

	@Override
	public TargetContactEvidence2D evaluateContact(LocusPoint2D point,
			LocusDifferentialEvaluation2D differential) {
		if (differential.getRegularity() != Regularity.REGULAR
				|| derivative == null || !evaluateFunction(point).established) {
			return TargetContactEvidence2D.notEstablished(
					"Function derivative or regular source differential is unavailable");
		}
		double slope;
		try {
			slope = derivative.value(point.getX());
		} catch (RuntimeException exception) {
			return TargetContactEvidence2D.notEstablished(
					"Function derivative evaluation failed");
		}
		LocusPoint2D tangent = differential.getDerivative();
		double speed = Math.hypot(tangent.getX(), tangent.getY());
		double normalLength = Math.hypot(slope, 1);
		if (!Double.isFinite(slope) || !Double.isFinite(speed)
				|| !Double.isFinite(normalLength) || speed == 0) {
			return TargetContactEvidence2D.notEstablished(
					"Function normal/source tangent normalization failed");
		}
		double indicator = (-slope * tangent.getX() + tangent.getY())
				/ (normalLength * speed);
		return TargetContactEvidence2D.established(indicator,
				"d(function-first-order-normal-level)/d(source-arc-length)",
				"Derivative-normalized function graph/source tangent factor; "
						+ "vertical residual remains authoritative");
	}

	private FunctionValue evaluateFunction(LocusPoint2D point) {
		if (point == null || !Double.isFinite(point.getX())
				|| !Double.isFinite(point.getY())) {
			return FunctionValue.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Function evaluation point is nonfinite");
		}
		if (!domain.containsX(point.getX(), 0)) {
			return FunctionValue.unavailable(
					TargetEvaluationStatus.OUTSIDE_EXPLICIT_DOMAIN,
					"Point x is outside the captured explicit function domain");
		}
		double value;
		try {
			value = function.value(point.getX());
		} catch (RuntimeException exception) {
			return FunctionValue.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Function evaluation failed inside its declared domain");
		}
		return Double.isFinite(value) ? FunctionValue.established(value)
				: FunctionValue.unavailable(
						TargetEvaluationStatus.TARGET_UNDEFINED,
						"Function is undefined or nonfinite at this x");
	}

	private static String requireIdentity(String identity) {
		if (identity == null || identity.trim().isEmpty()) {
			throw new IllegalArgumentException("Target identity is required");
		}
		return identity;
	}

	private static long requireStamp(long stamp) {
		if (stamp < 0) {
			throw new IllegalArgumentException("Target update stamp is invalid");
		}
		return stamp;
	}

	private static final class FunctionValue {
		private final boolean established;
		private final double value;
		private final TargetEvaluationStatus status;
		private final String diagnostic;

		private FunctionValue(boolean established, double value,
				TargetEvaluationStatus status, String diagnostic) {
			this.established = established;
			this.value = value;
			this.status = status;
			this.diagnostic = diagnostic;
		}

		static FunctionValue established(double value) {
			return new FunctionValue(true, value,
					TargetEvaluationStatus.ESTABLISHED,
					"Finite function evaluation established");
		}

		static FunctionValue unavailable(TargetEvaluationStatus status,
				String diagnostic) {
			return new FunctionValue(false, 0, status, diagnostic);
		}
	}
}

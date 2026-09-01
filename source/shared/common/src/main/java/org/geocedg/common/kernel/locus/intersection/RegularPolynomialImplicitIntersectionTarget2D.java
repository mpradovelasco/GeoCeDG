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
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;

/** Captured regular first-order adapter for a finite polynomial implicit curve. */
public final class RegularPolynomialImplicitIntersectionTarget2D
		implements PolynomialIntersectionTarget2D {
	private static final String ADAPTER_VERSION =
			"g8c1-regular-polynomial-implicit-normal-residual/v1";

	private final String targetIdentity;
	private final long targetUpdateStamp;
	private final double[][] coefficients;
	private final double contactOrientation;
	private final IntersectionResidualContract2D contract;

	/** Captures finite polynomial coefficients for one target revision. */
	public RegularPolynomialImplicitIntersectionTarget2D(
			GeoImplicitCurve target, String targetIdentity,
			long targetUpdateStamp) {
		java.util.Objects.requireNonNull(target);
		if (!target.isDefined()
				|| !hasFinitePolynomialCoefficients(target.getCoeff())) {
			throw new IllegalArgumentException(
					"G8C1 implicit adapter requires a nonzero finite polynomial");
		}
		this.targetIdentity = requireIdentity(targetIdentity);
		this.targetUpdateStamp = requireStamp(targetUpdateStamp);
		this.coefficients = copy(target.getCoeff());
		this.contactOrientation = LocusIntersectionTargets2D
				.canonicalPolynomialContactOrientation(coefficients);
		this.contract = new IntersectionResidualContract2D(ADAPTER_VERSION,
				ResidualQuantityKind.FIRST_ORDER_NORMAL_LENGTH,
				"model-coordinate",
				"captured polynomial G divided by hypot(dG/dx,dG/dy) at a "
						+ "regular point; first-order normal residual, not exact "
						+ "Euclidean distance",
				"unit model-coordinate length for unbounded polynomial target");
	}

	/** @return whether coefficients define a nonzero finite polynomial */
	static boolean hasFinitePolynomialCoefficients(double[][] input) {
		if (input == null || input.length == 0) {
			return false;
		}
		boolean nonzero = false;
		for (double[] row : input) {
			if (row == null) {
				return false;
			}
			for (double coefficient : row) {
				if (!Double.isFinite(coefficient)) {
					return false;
				}
				nonzero |= coefficient != 0;
			}
		}
		return nonzero;
	}

	@Override
	public TargetFamily getFamily() {
		return TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT;
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
	public double[][] getImplicitPolynomialCoefficients() {
		return copy(coefficients);
	}

	@Override
	public TargetCandidateEvaluation2D evaluateCandidateLevel(
			LocusPoint2D point) {
		RegularResidual value = regularResidual(point);
		return value.established
				? TargetCandidateEvaluation2D.established(value.normalized, 1,
						"Regular polynomial first-order candidate level")
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
		RegularResidual value = regularResidual(point);
		if (!value.established) {
			return TargetResidualEvaluation2D.unavailable(value.status,
					value.diagnostic);
		}
		return TargetResidualEvaluation2D.established(
				new TargetResidual2D(value.raw, value.gradientNorm,
						value.normalized, 1, contract),
				"Independent regular polynomial implicit residual verification");
	}

	@Override
	public TargetMembership2D evaluateMembership(LocusPoint2D point,
			double coordinateTolerance) {
		boolean regular = regularResidual(point).established;
		return new TargetMembership2D(regular ? MembershipStatus.MEMBER
				: MembershipStatus.NOT_ESTABLISHED, false,
				regular ? "Regular polynomial residual establishes local membership"
						: "Singular/nonfinite polynomial point is outside the "
								+ "regular adapter contract");
	}

	@Override
	public TargetContactEvidence2D evaluateContact(LocusPoint2D point,
			LocusDifferentialEvaluation2D differential) {
		if (differential.getRegularity() != Regularity.REGULAR) {
			return TargetContactEvidence2D.notEstablished(
					"Source differential is not established regular");
		}
		double gradientX = derivativeX(point.getX(), point.getY());
		double gradientY = derivativeY(point.getX(), point.getY());
		double gradientNorm = Math.hypot(gradientX, gradientY);
		LocusPoint2D tangent = differential.getDerivative();
		double speed = Math.hypot(tangent.getX(), tangent.getY());
		if (!Double.isFinite(gradientNorm) || !Double.isFinite(speed)
				|| gradientNorm == 0 || speed == 0) {
			return TargetContactEvidence2D.notEstablished(
					"Implicit normal/source tangent normalization failed");
		}
		double indicator = contactOrientation * (gradientX * tangent.getX()
				+ gradientY * tangent.getY()) / (gradientNorm * speed);
		return TargetContactEvidence2D.established(indicator,
				"d(canonical-implicit-normal-level)/d(source-arc-length)",
				"Canonical captured polynomial gradient/source tangent factor");
	}

	private RegularResidual regularResidual(LocusPoint2D point) {
		if (point == null || !Double.isFinite(point.getX())
				|| !Double.isFinite(point.getY())) {
			return RegularResidual.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Implicit evaluation point is nonfinite");
		}
		double raw = evaluate(point.getX(), point.getY());
		double gradientNorm = Math.hypot(derivativeX(point.getX(), point.getY()),
				derivativeY(point.getX(), point.getY()));
		if (!Double.isFinite(raw) || !Double.isFinite(gradientNorm)) {
			return RegularResidual.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Polynomial value or gradient is nonfinite");
		}
		if (gradientNorm == 0) {
			return RegularResidual.unavailable(
					TargetEvaluationStatus.UNSUPPORTED_LOCAL_GEOMETRY,
					"Polynomial gradient is zero; singular point is outside "
							+ "the regular adapter contract");
		}
		return RegularResidual.established(raw, gradientNorm,
				raw / gradientNorm);
	}

	private double evaluate(double x, double y) {
		double value = 0;
		double xPower = 1;
		for (double[] row : coefficients) {
			double yPower = 1;
			for (double coefficient : row) {
				value += coefficient * xPower * yPower;
				yPower *= y;
			}
			xPower *= x;
		}
		return value;
	}

	private double derivativeX(double x, double y) {
		double value = 0;
		double xPower = 1;
		for (int xDegree = 1; xDegree < coefficients.length; xDegree++) {
			double yPower = 1;
			for (double coefficient : coefficients[xDegree]) {
				value += xDegree * coefficient * xPower * yPower;
				yPower *= y;
			}
			xPower *= x;
		}
		return value;
	}

	private double derivativeY(double x, double y) {
		double value = 0;
		double xPower = 1;
		for (double[] row : coefficients) {
			double yPower = 1;
			for (int yDegree = 1; yDegree < row.length; yDegree++) {
				value += yDegree * row[yDegree] * xPower * yPower;
				yPower *= y;
			}
			xPower *= x;
		}
		return value;
	}

	private static double[][] copy(double[][] input) {
		double[][] result = new double[input.length][];
		for (int index = 0; index < input.length; index++) {
			result[index] = input[index].clone();
		}
		return result;
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

	private static final class RegularResidual {
		private final boolean established;
		private final double raw;
		private final double gradientNorm;
		private final double normalized;
		private final TargetEvaluationStatus status;
		private final String diagnostic;

		private RegularResidual(boolean established, double raw,
				double gradientNorm, double normalized,
				TargetEvaluationStatus status, String diagnostic) {
			this.established = established;
			this.raw = raw;
			this.gradientNorm = gradientNorm;
			this.normalized = normalized;
			this.status = status;
			this.diagnostic = diagnostic;
		}

		static RegularResidual established(double raw, double gradientNorm,
				double normalized) {
			return new RegularResidual(true, raw, gradientNorm, normalized,
					TargetEvaluationStatus.ESTABLISHED,
					"Regular first-order residual established");
		}

		static RegularResidual unavailable(TargetEvaluationStatus status,
				String diagnostic) {
			return new RegularResidual(false, 0, 0, 0, status, diagnostic);
		}
	}
}
